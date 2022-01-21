package org.example.service;

import java.util.Map;
import org.example.controller.request.ConfirmOrderRequest;
import org.example.util.JsonData;

public interface ProductOrderService {

    Map<String, Object> page(int page, int size, String state);

    String queryProductOrderState(String outTradeNo);

    JsonData confirmOrder(ConfirmOrderRequest orderRequest);
}
