package org.example.controller;


import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.example.annotation.RepeatSubmit;
import org.example.constant.RedisKey;
import org.example.controller.request.ConfirmOrderRequest;
import org.example.controller.request.ProductOrderPageRequest;
import org.example.enums.BizCodeEnum;
import org.example.enums.ClientTypeEnum;
import org.example.enums.ProductOrderPayTypeEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.service.ProductOrderService;
import org.example.util.CommonUtil;
import org.example.util.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/v1")
@Slf4j
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 下单前获取令牌用于防重提交
     */
    @GetMapping("token")
    public JsonData getOrderToken() {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        String token = CommonUtil.getStringNumRandom(32);
        String key = String.format(RedisKey.SUBMIT_ORDER_TOKEN_KEY, accountNo, token);
        //令牌有效时间是30分钟
        redisTemplate.opsForValue().set(key, String.valueOf(Thread.currentThread().getId()), 30, TimeUnit.MINUTES);
        return JsonData.buildSuccess(token);
    }

    /**
     * 分页接口
     */
    @GetMapping("page")
    @RepeatSubmit(limitType = RepeatSubmit.Type.PARAM)
    public JsonData page(@RequestBody ProductOrderPageRequest orderPageRequest) {
        Map<String, Object> pageResult = productOrderService.page(orderPageRequest);
        return JsonData.buildSuccess(pageResult);
    }

    /**
     * 查询订单状态
     */
    @GetMapping("query_state")
    public JsonData queryState(@RequestParam(value = "out_trade_no") String outTradeNo) {
        String state = productOrderService.queryProductOrderState(outTradeNo);
        return StringUtils.isBlank(state)
                ? JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST)
                : JsonData.buildSuccess(state);
    }

    /**
     * 下单接口
     */
    @PostMapping("confirm")
    public void confirmOrder(@RequestBody ConfirmOrderRequest orderRequest, HttpServletResponse response) {
        JsonData jsonData = productOrderService.confirmOrder(orderRequest);
         if (jsonData.getCode() == 0) {
            //客户端类型
            String client = orderRequest.getClientType();
            //支付类型
            String payType = orderRequest.getPayType();
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALI_PAY.name())) {
                //如果是支付宝支付，跳转网页，sdk除非
                if (client.equalsIgnoreCase(ClientTypeEnum.PC.name())) {
                    CommonUtil.sendHtmlMessage(response, jsonData);
                } else if (client.equalsIgnoreCase(ClientTypeEnum.APP.name())) {
                } else if (client.equalsIgnoreCase(ClientTypeEnum.H5.name())) {
                }
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT_PAY.name())) {
                //微信支付
                CommonUtil.sendJsonMessage(response, jsonData);
            }

        } else {
            log.error("创建订单失败{}", jsonData.toString());
            CommonUtil.sendJsonMessage(response, jsonData);
        }

    }


}

