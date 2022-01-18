package org.example.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.controller.request.ShortLinkAddRequest;
import org.example.controller.request.ShortLinkDelRequest;
import org.example.controller.request.ShortLinkPageRequest;
import org.example.controller.request.ShortLinkUpdateRequest;
import org.example.enums.DomainTypeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.ShortLinkStateEnum;
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
        //为原始URL拼接前缀，使得一个原始URL可以对应多个短链码
        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);
        EventMessage eventMessage = EventMessage.builder()
            .messageId(IDUtil.geneSnowFlakeID().toString())
            .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
            .accountNo(accountNo)
            .content(JsonUtil.obj2Json(request))
            .build();

        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),
            rabbitMQConfig.getShortLinkAddRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
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
    public boolean handlerAddShortLink(EventMessage eventMessage) {
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

        //加锁成功
        if (result > 0) {
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
                // C端添加短链码信息
                ShortLinkDO shortLinCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);
                if (shortLinCodeDOInDB == null) {
                    ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .accountNo(accountNo).code(shortLinkCode)
                        .title(addRequest.getTitle()).originalUrl(addRequest.getOriginalUrl())
                        .domain(domainDO.getValue()).groupId(linkGroupDO.getId())
                        .expired(addRequest.getExpired()).sign(originalUrlDigest)
                        .state(ShortLinkStateEnum.ACTIVE.name()).del(0).build();
                    shortLinkManager.addShortLink(shortLinkDO);
                    return true;
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
            //加锁失败，自旋100毫秒，再调用； 失败的可能是短链码已经被占用，需要重新生成
            log.error("加锁失败:{}", eventMessage);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
            duplicateCodeFlag = true;
        }
        if (duplicateCodeFlag) {
            String newOriginalUrl = CommonUtil.addUrlPrefixVersion(addRequest.getOriginalUrl());
            addRequest.setOriginalUrl(newOriginalUrl);
            eventMessage.setContent(JsonUtil.obj2Json(addRequest));
            log.warn("短链码报错失败，重新生成:{}", eventMessage);
            handlerAddShortLink(eventMessage);
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
        //TODO
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
        //TODO
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
