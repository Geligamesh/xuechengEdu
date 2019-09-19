package com.xuecheng.test.rabbitmq.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class ReceiveHandler {

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_INFORM_EMAIL})
    public void receive_email(String msg, Message message, Channel channel) throws UnsupportedEncodingException {
        System.out.println("receive message is:" + msg);
        System.out.println("receive message args:" + new String(message.getBody(),"utf-8"));
    }
}
