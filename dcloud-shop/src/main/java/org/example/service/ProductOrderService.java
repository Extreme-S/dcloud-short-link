package org.example.service;

import java.util.Map;
import org.example.controller.request.ConfirmOrderRequest;
import org.example.controller.request.ProductOrderPageRequest;
import org.example.enums.ProductOrderPayTypeEnum;
import org.example.model.EventMessage;
import org.example.util.JsonData;

public interface ProductOrderService {

    Map<String, Object> page(ProductOrderPageRequest orderPageRequest);

    String queryProductOrderState(String outTradeNo);

    JsonData confirmOrder(ConfirmOrderRequest orderRequest);

    boolean closeProductOrder(EventMessage eventMessage);

    JsonData processOrderCallbackMsg(ProductOrderPayTypeEnum wechatPay, Map<String, String> paramsMap);
}
