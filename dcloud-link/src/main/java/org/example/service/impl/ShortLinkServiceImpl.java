package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.controller.request.ShortLinkAddRequest;
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
        ShortLinkDO shortLinkDO = shortLinkManager.findByShortLinCode(shortLinkCode);
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
        //处理原始URL，拼接前缀，使得一个原始URL可以对应对歌短链码
        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);
        EventMessage eventMessage = EventMessage.builder()
            .accountNo(accountNo)
            .content(JsonUtil.obj2Json(request))
            .messageId(IDUtil.geneSnowFlakeID().toString())
            .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
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

        //TODO 加锁

        //先判断是否短链码被占用
        ShortLinkDO ShortLinCodeDOInDB = shortLinkManager.findByShortLinCode(shortLinkCode);
        if (ShortLinCodeDOInDB == null) {
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {//C端处理
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .accountNo(accountNo)
                    .code(shortLinkCode)
                    .title(addRequest.getTitle())
                    .originalUrl(addRequest.getOriginalUrl())
                    .domain(domainDO.getValue())
                    .groupId(linkGroupDO.getId())
                    .expired(addRequest.getExpired())
                    .sign(originalUrlDigest)
                    .state(ShortLinkStateEnum.ACTIVE.name())
                    .del(0)
                    .build();
                shortLinkManager.addShortLink(shortLinkDO);
                return true;
            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {//B端处理
                GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .accountNo(accountNo)
                    .code(shortLinkCode)
                    .title(addRequest.getTitle())
                    .originalUrl(addRequest.getOriginalUrl())
                    .domain(domainDO.getValue())
                    .groupId(linkGroupDO.getId())
                    .expired(addRequest.getExpired())
                    .sign(originalUrlDigest)
                    .state(ShortLinkStateEnum.ACTIVE.name())
                    .del(0)
                    .build();
                groupCodeMappingManager.add(groupCodeMappingDO);
                return true;
            }
        }
        return true;
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
