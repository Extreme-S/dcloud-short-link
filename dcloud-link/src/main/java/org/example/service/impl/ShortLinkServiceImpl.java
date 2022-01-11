package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.controller.request.ShortLinkAddRequest;
import org.example.enums.EventMessageType;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.ShortLinkManager;
import org.example.model.EventMessage;
import org.example.model.ShortLinkDO;
import org.example.service.ShortLinkService;
import org.example.util.IDUtil;
import org.example.util.JsonData;
import org.example.util.JsonUtil;
import org.example.vo.ShortLinkVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ShortLinkServiceImpl implements ShortLinkService {

    @Autowired
    private ShortLinkManager shortLinkManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

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
        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo)
            .content(JsonUtil.obj2Json(request))
            .messageId(IDUtil.geneSnowFlakeID().toString())
            .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
            .build( );

        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),
            rabbitMQConfig.getShortLinkAddRoutingKey(), eventMessage);
        return JsonData.buildSuccess();
    }
}
