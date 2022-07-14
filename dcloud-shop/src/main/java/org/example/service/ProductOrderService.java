package org.example.service;

import java.util.Map;
import org.example.controller.request.ConfirmOrderRequest;
import org.example.controller.request.ProductOrderPageRequest;
import org.example.util.JsonData;

public interface ProductOrderService {

    Map<String, Object> page(ProductOrderPageRequest orderPageRequest);

    String queryProductOrderState(String outTradeNo);

    JsonData confirmOrder(ConfirmOrderRequest orderRequest);
}
