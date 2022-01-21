package org.example.manager;

import java.util.Map;
import org.example.model.ProductOrderDO;

public interface ProductOrderManager {

    /***
     * 新增
     */
    int add(ProductOrderDO productOrderDO);

    /**
     * 通过订单号和账号查询
     */
    ProductOrderDO findByOutTradeNoAndAccountNo(String outTradeNo, Long accountNo);

    /**
     * 更新订单状态
     */
    int updateOrderPayState(String outTradeNo, Long accountNo, String newState, String oldState);

    /**
     * 分页查看订单列表
     */
    Map<String, Object> page(int page, int size, Long accountNo, String state);

    /**
     * 删除
     */
    int del(Long productOrderId, Long accountNo);

}
