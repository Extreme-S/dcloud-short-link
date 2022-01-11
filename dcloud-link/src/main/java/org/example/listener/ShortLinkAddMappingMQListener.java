package org.example.listener;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.model.EventMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queues = "short_link.add.mapping.queue")
public class ShortLinkAddMappingMQListener {

    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ShortLinkAddMappingMQListener message消息内容:{}", message);
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            //TODO 处理业务逻辑
            int i = 1 / 0;
        } catch (Exception e) {
            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        //确认消息消费成功
        log.info("消费成功:{}", eventMessage);
        //channel.basicAck(tag, false);
    }


}
