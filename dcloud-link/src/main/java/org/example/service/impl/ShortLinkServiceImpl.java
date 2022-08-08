package org.example.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.constant.LuaScript;
import org.example.constant.RedisKey;
import org.example.controller.request.*;
import org.example.enums.BizCodeEnum;
import org.example.enums.DomainTypeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.ShortLinkStateEnum;
import org.example.feign.TrafficFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.DomainManager;
import org.example.manager.GroupCodeMappingManager;
import org.example.manager.LinkGroupManager;
import org.example.manager.ShortLinkManager;
import org.example.model.DomainDO;
import org.example.model.EventMessage;
import org.example.model.GroupCodeMappingDO;
import org.example.model.LinkGroupDO;
import org.example.model.ShortLinkDO;
import org.example.service.ShortLinkService;
import org.example.util.CommonUtil;
import org.example.util.IDUtil;
import org.example.util.JsonData;
import org.example.util.JsonUtil;
import org.example.vo.ShortLinkVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
@Slf4j
public class ShortLinkServiceImpl implements ShortLinkService {

    @Autowired
    private ShortLinkManager shortLinkManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private DomainManager domainManager;

    @Autowired
    private LinkGroupManager linkGroupManager;

    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Autowired
    private GroupCodeMappingManager groupCodeMappingManager;

    @Autowired
    private TrafficFeignService trafficFeignService;


    @Override
    public ShortLinkVO parseShortLinkCode(String shortLinkCode) {
        ShortLinkDO shortLinkDO = shortLinkManager.findByShortLinkCode(shortLinkCode);
        if (shortLinkDO == null) {
            return null;
        }
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        BeanUtils.copyProperties(shortLinkDO, shortLinkVO);
        return shortLinkVO;
    }

    @Override
    public JsonData createShortLink(ShortLinkAddRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        //需要预先检查下是否有足够多的可以进行创建
        String cacheKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC, accountNo);
        // 检查key是否存在，然后递减，是否大于等于0，使用lua脚本
        // 如果key不存在，则未使用过，lua返回值是0； 新增流量包的时候，不用重新计算次数，直接删除key,消费的时候回计算更新
//        String script = "if redis.call('get',KEYS[1]) then return redis.call('decr',KEYS[1]) else return 0 end";
        Long leftTimes = redisTemplate.execute(
                new DefaultRedisScript<>(LuaScript.USER_DAY_TOTAL_TRAFFIC_DECR, Long.class), List.of(cacheKey), "");
        log.info("今日流量包剩余次数:{}", leftTimes);
        if (leftTimes >= 0) {
            String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
            request.setOriginalUrl(newOriginalUrl);
            EventMessage eventMessage = EventMessage.builder().accountNo(accountNo)
                    .content(JsonUtil.obj2Json(request))
                    .messageId(IDUtil.geneSnowFlakeID().toString())
                    .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                    .build();
            rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkAddRoutingKey(), eventMessage);
            return JsonData.buildSuccess();
        } else {
            //流量包不足
            return JsonData.buildResult(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        }


    }

    /**
     * 处理短链新增逻辑
     * 1、生成长链摘要
     * 2、判断短链域名是否合法
     * 3、判断组名是否合法
     * 4、生成短链码
     * 5、加锁（加锁再查，不然查询后，加锁前有线程刚好新增）
     * 6、查询短链码是否存在
     * 7、构建短链mapping对象
     * 8、保存数据库
     */
    @Override
    public boolean handleAddShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkAddRequest addRequest = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddRequest.class);
        //数据校验：1、短链域名；2、短链组名；
        DomainDO domainDO = checkDomain(addRequest.getDomainType(), addRequest.getDomainId(), accountNo);
        LinkGroupDO linkGroupDO = checkLinkGroup(addRequest.getGroupId(), accountNo);
        //MD5长链摘要，生成短链码，构造shortLinkDO对象
        String originalUrlDigest = CommonUtil.MD5(addRequest.getOriginalUrl());
        String shortLinkCode = shortLinkComponent.createShortLinkCode(addRequest.getOriginalUrl());

        // key1是短链码，ARGV[1]是accountNo,ARGV[2]是过期时间
        String script = "if redis.call('EXISTS',KEYS[1])==0 then " +
                "redis.call('set',KEYS[1],ARGV[1]); " +
                "redis.call('expire',KEYS[1],ARGV[2]); " +
                "return 1; " +
                "elseif redis.call('get',KEYS[1]) == ARGV[1] then " +
                "return 2; " +
                "else return 0; end;";
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class), List.of(shortLinkCode), accountNo, 100);
        boolean duplicateCodeFlag = false;
        if (result > 0) {
            //如果加锁成功
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
                // C端添加短链码
                ShortLinkDO shortLinCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);//判断短链码是否被占用
                if (shortLinCodeDOInDB == null) {
                    boolean reduceFlag = reduceTraffic(eventMessage, shortLinkCode);
                    if (reduceFlag) {
                        ShortLinkDO shortLinkDO = ShortLinkDO.builder().accountNo(accountNo).code(shortLinkCode)
                                .title(addRequest.getTitle()).originalUrl(addRequest.getOriginalUrl())
                                .domain(domainDO.getValue()).groupId(linkGroupDO.getId()).expired(addRequest.getExpired())
                                .sign(originalUrlDigest).state(ShortLinkStateEnum.ACTIVE.name()).del(0).build();
                        shortLinkManager.addShortLink(shortLinkDO);
                        return true;
                    }
                } else {
                    log.error("C端短链码重复:{}", eventMessage);
                    duplicateCodeFlag = true;
                }
            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {
                // B端添加短链码的映射关系
                GroupCodeMappingDO groupCodeMappingDOInDB = groupCodeMappingManager.findByCodeAndGroupId(
                        shortLinkCode, linkGroupDO.getId(), accountNo);
                if (groupCodeMappingDOInDB == null) {
                    GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                            .accountNo(accountNo).code(shortLinkCode).title(addRequest.getTitle())
                            .originalUrl(addRequest.getOriginalUrl())
                            .domain(domainDO.getValue()).groupId(linkGroupDO.getId())
                            .expired(addRequest.getExpired()).sign(originalUrlDigest)
                            .state(ShortLinkStateEnum.ACTIVE.name()).del(0).build();
                    groupCodeMappingManager.add(groupCodeMappingDO);
                    return true;
                } else {
                    log.error("B端短链码重复:{}", eventMessage);
                    duplicateCodeFlag = true;
                }
            }
        } else {
            //加锁失败，自旋100毫秒，再调用；失败的可能是短链码已经被占用，需要重新生成
            log.error("加锁失败:{}", eventMessage);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {
            }
            duplicateCodeFlag = true;
        }
        if (duplicateCodeFlag) {
            String newOriginalUrl = CommonUtil.addUrlPrefixVersion(addRequest.getOriginalUrl());
            addRequest.setOriginalUrl(newOriginalUrl);
            eventMessage.setContent(JsonUtil.obj2Json(addRequest));
            log.warn("短链码报错失败，重新生成:{}", eventMessage);
            handleAddShortLink(eventMessage);
        }
        return false;
    }

    /**
     * 扣减流量包
     */
    private boolean reduceTraffic(EventMessage eventMessage, String shortLinkCode) {
        UseTrafficRequest request = new UseTrafficRequest(eventMessage.getAccountNo(), shortLinkCode);
        JsonData jsonData = trafficFeignService.useTraffic(request);
        if (jsonData.getCode() != 0) {
            log.error("流量包不足，扣减失败:{}", eventMessage);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleUpdateShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkUpdateRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkUpdateRequest.class);
        //校验短链域名
        DomainDO domainDO = checkDomain(request.getDomainType(), request.getDomainId(), accountNo);
        if (EventMessageType.SHORT_LINK_UPDATE_LINK.name().equalsIgnoreCase(messageType)) {
            //C端处理
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(request.getCode())
                    .title(request.getTitle())
                    .domain(domainDO.getValue())
                    .accountNo(accountNo)
                    .build();
            int rows = shortLinkManager.update(shortLinkDO);
            log.debug("更新C端短链，rows={}", rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_UPDATE_MAPPING.name().equalsIgnoreCase(messageType)) {
            //B端处理
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(request.getMappingId())
                    .groupId(request.getGroupId())
                    .accountNo(accountNo)
                    .title(request.getTitle())
                    .domain(domainDO.getValue())
                    .build();
            int rows = groupCodeMappingManager.update(groupCodeMappingDO);
            log.debug("更新B端短链，rows={}", rows);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleDelShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkDelRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkDelRequest.class);
        if (EventMessageType.SHORT_LINK_DEL_LINK.name().equalsIgnoreCase(messageType)) {
            //C端解析
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(request.getCode())
                    .accountNo(accountNo)
                    .build();
            int rows = shortLinkManager.del(shortLinkDO);
            log.debug("删除C端短链:{}", rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_DEL_MAPPING.name().equalsIgnoreCase(messageType)) {
            //B端处理
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(request.getMappingId())
                    .accountNo(accountNo)
                    .groupId(request.getGroupId())
                    .build();
            int rows = groupCodeMappingManager.del(groupCodeMappingDO);
            log.debug("删除B端短链:{}", rows);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> pageByGroupId(ShortLinkPageRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return groupCodeMappingManager.pageShortLinkByGroupId(
                request.getPage(), request.getSize(), accountNo, request.getGroupId());
    }


    @Override
    public JsonData del(ShortLinkDelRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        EventMessage eventMessage = EventMessage.builder()
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_DEL.name())
                .build();
        // 发送删除消息
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkDelRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
    }


    @Override
    public JsonData update(ShortLinkUpdateRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        EventMessage eventMessage = EventMessage.builder()
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_UPDATE.name())
                .build();
        // 发送更新消息
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkUpdateRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
    }

    /**
     * 校验短链域名是否合法
     */
    private DomainDO checkDomain(String domainType, Long domainId, Long accountNo) {
        DomainDO domainDO;
        if (DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)) {
            domainDO = domainManager.findById(domainId, accountNo);
        } else {
            domainDO = domainManager.findByDomainTypeAndID(domainId, DomainTypeEnum.OFFICIAL);
        }
        Assert.notNull(domainDO, "短链域名不合法");
        return domainDO;
    }

    /**
     * 校验组名
     */
    private LinkGroupDO checkLinkGroup(Long groupId, Long accountNo) {
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        Assert.notNull(linkGroupDO, "组名不合法");
        return linkGroupDO;
    }
}
