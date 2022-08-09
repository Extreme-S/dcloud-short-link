package org.example.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.model.EventMessage;
import org.example.service.TrafficService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@RabbitListener(queuesToDeclare = {
        @Queue("order.traffic.queue"),
        @Queue("traffic.free_init.queue"),
        @Queue("traffic.release.queue")
})
@Slf4j
public class TrafficMQListener {

    @Autowired
    private TrafficService trafficService;

    @RabbitHandler
    public void trafficHandler(EventMessage eventMessage, Message message, Channel channel) {
        log.info("监听到消息trafficHandler:{}", eventMessage);
        try {
            trafficService.handleTrafficMessage(eventMessage);
        } catch (Exception e) {
            log.error("消费者失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);
    }

}
