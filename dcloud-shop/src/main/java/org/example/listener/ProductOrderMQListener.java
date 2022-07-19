package org.example.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.model.EventMessage;
import org.example.service.ProductOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queuesToDeclare = {@Queue("order.close.queue")})
public class ProductOrderMQListener {

    @Autowired
    private ProductOrderService productOrderService;

    @RabbitHandler
    public void productOrderHandler(EventMessage eventMessage, Message message, Channel channel) {
        log.info("监听到消息ProductOrderMQListener message消息内容:{}", message);
        try {
            //关闭订单
            productOrderService.closeProductOrder(eventMessage);
        } catch (Exception e) {
            log.error("消费者失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);
    }


}
