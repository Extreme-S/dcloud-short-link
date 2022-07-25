package org.example.component;

import org.example.vo.PayInfoVO;


public class PayStrategyContext  {

    private PayStrategy payStrategy;

    public PayStrategyContext(PayStrategy payStrategy){
        this.payStrategy = payStrategy;
    }


    /**
     * 根据策略对象，执行不同的下单接口
     */
    public String executeUnifiedOrder(PayInfoVO payInfoVO){
        return payStrategy.unifiedOrder(payInfoVO);
    }


    /**
     * 根据策略对象，执行不同的退款接口*/
    public String executeRefund(PayInfoVO payInfoVO){
        return payStrategy.refund(payInfoVO);
    }


    /**
     * 根据策略对象，执行不同的关闭接口
     */
    public String executeCloseOrder(PayInfoVO payInfoVO){
        return payStrategy.closeOrder(payInfoVO);
    }


    /**
     * 根据策略对象，执行不同的查询订单状态接口
     */
    public String executeQueryPayStatus(PayInfoVO payInfoVO){
        return payStrategy.queryPayStatus(payInfoVO);
    }


}
