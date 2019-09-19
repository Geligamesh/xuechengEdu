package com.xuecheng;

import com.xuecheng.test.rabbitmq.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Product06_topics_springboot {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendEmail() {

        /**
         * 参数：
         * 1.交换机名称
         * 2.routingKey
         * 3.消息内容
         */
        String message = "send email message to user";
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TOPICS_INFORM, "inform.email", message);
    }
}
