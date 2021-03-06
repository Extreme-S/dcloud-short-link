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
    private EventMessage eventMessage;


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
        //?????????URL?????????????????????????????????URL???????????????????????????
        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);
        EventMessage eventMessage = EventMessage.builder()
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request)).build();
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),
                rabbitMQConfig.getShortLinkAddRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
    }

    /**
     * ????????????????????????
     * 1?????????????????????
     * 2?????????????????????????????????
     * 3???????????????????????????
     * 4??????????????????
     * 5??????????????????????????????????????????????????????????????????????????????
     * 6??????????????????????????????
     * 7???????????????mapping??????
     * 8??????????????????
     */
    @Override
    public boolean handleAddShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkAddRequest addRequest = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddRequest.class);
        //???????????????1??????????????????2??????????????????
        DomainDO domainDO = checkDomain(addRequest.getDomainType(), addRequest.getDomainId(), accountNo);
        LinkGroupDO linkGroupDO = checkLinkGroup(addRequest.getGroupId(), accountNo);
        //MD5???????????????????????????????????????shortLinkDO??????
        String originalUrlDigest = CommonUtil.MD5(addRequest.getOriginalUrl());
        String shortLinkCode = shortLinkComponent.createShortLinkCode(addRequest.getOriginalUrl());

        // key1???????????????ARGV[1]???accountNo,ARGV[2]???????????????
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

        //????????????
        if (result > 0) {
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
                // C????????????????????????
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
                    log.error("C??????????????????:{}", eventMessage);
                    duplicateCodeFlag = true;
                }
            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {
                // B?????????????????????????????????
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
                    log.error("B??????????????????:{}", eventMessage);
                    duplicateCodeFlag = true;
                }
            }
        } else {
            //?????????????????????100????????????????????? ???????????????????????????????????????????????????????????????
            log.error("????????????:{}", eventMessage);
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
            log.warn("????????????????????????????????????:{}", eventMessage);
            handleAddShortLink(eventMessage);
        }
        return false;
    }

    @Override
    public boolean handleUpdateShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkUpdateRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkUpdateRequest.class);
        //??????????????????
        DomainDO domainDO = checkDomain(request.getDomainType(), request.getDomainId(), accountNo);
        if (EventMessageType.SHORT_LINK_UPDATE_LINK.name().equalsIgnoreCase(messageType)) {
            //C?????????
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(request.getCode())
                    .title(request.getTitle())
                    .domain(domainDO.getValue())
                    .accountNo(accountNo)
                    .build();
            int rows = shortLinkManager.update(shortLinkDO);
            log.debug("??????C????????????rows={}", rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_UPDATE_MAPPING.name().equalsIgnoreCase(messageType)) {
            //B?????????
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(request.getMappingId())
                    .groupId(request.getGroupId())
                    .accountNo(accountNo)
                    .title(request.getTitle())
                    .domain(domainDO.getValue())
                    .build();
            int rows = groupCodeMappingManager.update(groupCodeMappingDO);
            log.debug("??????B????????????rows={}", rows);
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
            //C?????????
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(request.getCode())
                    .accountNo(accountNo)
                    .build();
            int rows = shortLinkManager.del(shortLinkDO);
            log.debug("??????C?????????:{}", rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_DEL_MAPPING.name().equalsIgnoreCase(messageType)) {
            //B?????????
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(request.getMappingId())
                    .accountNo(accountNo)
                    .groupId(request.getGroupId())
                    .build();
            int rows = groupCodeMappingManager.del(groupCodeMappingDO);
            log.debug("??????B?????????:{}", rows);
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
        // ??????????????????
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
        // ??????????????????
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkUpdateRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
    }

    /**
     * ??????????????????????????????
     */
    private DomainDO checkDomain(String domainType, Long domainId, Long accountNo) {
        DomainDO domainDO;
        if (DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)) {
            domainDO = domainManager.findById(domainId, accountNo);
        } else {
            domainDO = domainManager.findByDomainTypeAndID(domainId, DomainTypeEnum.OFFICIAL);
        }
        Assert.notNull(domainDO, "?????????????????????");
        return domainDO;
    }

    /**
     * ????????????
     */
    private LinkGroupDO checkLinkGroup(Long groupId, Long accountNo) {
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        Assert.notNull(linkGroupDO, "???????????????");
        return linkGroupDO;
    }
}
