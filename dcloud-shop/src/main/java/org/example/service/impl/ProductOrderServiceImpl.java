package org.example.service.impl;

import java.util.Map;
import org.example.controller.request.ConfirmOrderRequest;
import org.example.interceptor.LoginInterceptor;
import org.example.manager.ProductOrderManager;
import org.example.model.ProductOrderDO;
import org.example.service.ProductOrderService;
import org.example.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProductOrderServiceImpl implements ProductOrderService {

    @Autowired
    private ProductOrderManager productOrderManager;

    @Override
    public Map<String, Object> page(int page, int size, String state) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return productOrderManager.page(page, size, accountNo, state);
    }

    @Override
    public String queryProductOrderState(String outTradeNo) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        ProductOrderDO productOrderDO = productOrderManager.findByOutTradeNoAndAccountNo(outTradeNo, accountNo);
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }


    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        return null;
    }


}

