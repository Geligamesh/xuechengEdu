package com.xuecheng;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Product04_topics {

    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    private static final String ROUTINGKEY_EMAIL = "inform.#.email.#";
    private static final String ROUTINGKEY_SMS = "inform.#.sms.#";

    public static void main(String[] args) {
        //通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每个虚拟机就相当于一个独立的mq
        factory.setVirtualHost("/");
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
            channel = connection.createChannel();
            //声明队列，如果队列在mq 中没有则要创建
            //参数：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /**
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //声明一个交换机
            /**
             * 参数明细:
             * 1.交换机的名称
             * 2.交换机的类型
             * fanout:对应的rabbit的工作模式是publish/subscribe(发布订阅)
             * direct:对应的Routing的工作模式
             * topic:通配符工作模式
             * headers:Header工作模式
             * RPC:远程调用
             */
            //发布订阅模式
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //进行交换机和队列绑定
            /**
             * 参数明细：
             * 1.队列名称
             * 2.交换机名称
             * 3.routingKey 路由key，在发布订阅模式中调协为空字符串
             * 作用是交换机根据路由key的值将消息转发到指定的队列中
             */
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_TOPICS_INFORM, ROUTINGKEY_EMAIL);
            // channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_TOPICS_INFORM, "inform");
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, ROUTINGKEY_SMS);
            // channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, "inform");

            /**
             * 参数明细：
             * 1、exchange，交换机，如果不指定将使用mq的默认交换机（设置为""）
             * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认交换机，routingKey设置为队列的名称
             * 3、props，消息的属性
             * 4、body，消息内容
             */
            //消息内容
            String message = "send email message to user";
            for (int i = 0; i < 5; i++) {
                //发送消息的时候要指定routingKey
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.email", null, message.getBytes());
                System.out.println("send to mq "+message);
            }
            for (int i = 0; i < 5; i++) {
                //发送消息的时候要指定routingKey
                message = "send sms message to user";
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms", null, message.getBytes());
                System.out.println("send to mq "+message);
            }
            // for (int i = 0; i < 5; i++) {
            //     //发送消息的时候要指定routingKey
            //     message = "send email and sms to user";
            //     channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms.email", null, message.getBytes());
            //     System.out.println("send to mq "+message);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
