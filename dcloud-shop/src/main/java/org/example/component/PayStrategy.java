package org.example.component;

import org.example.vo.PayInfoVO;

public interface PayStrategy {

    /**
     * 统一下单接口
     */
    String unifiedOrder(PayInfoVO payInfoVO);


    /**
     * 退款接口
     */
    default String refund(PayInfoVO payInfoVO){ return ""; }


    /**
     * 查询支付状态
     */
    default String queryPayStatus(PayInfoVO payInfoVO){ return ""; }


    /**
     * 关闭订单
     */
    default String closeOrder(PayInfoVO payInfoVO){ return ""; }

}
