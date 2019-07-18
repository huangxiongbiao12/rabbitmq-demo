package com.example.demo;

import com.example.demo.mq.MQProperties;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 参考：https://my.oschina.net/dengfuwei/blog/1595047
 * 监听的方法内部必须使用channel进行消息确认，包括消费成功或消费失败
 *   1、如果不手动确认，也不抛出异常，消息不会自动重新推送（包括其他消费者），因为对于rabbitmq来说始终没有接收到消息消费是否成功的确认，并且Channel是在消费端有缓存的，没有断开连接
 *   2、如果rabbitmq断开，连接后会自动重新推送（不管是网络问题还是宕机）
 *   3、如果消费端应用重启，消息会自动重新推送
 *   4、如果消费端处理消息的时候宕机，消息会自动推给其他的消费者
 *   5、如果监听消息的方法抛出异常，消息会按照listener.retry的配置进行重发，但是重发次数完了之后还抛出异常的话，消息不会重发（也不会重发到其他消费者），只有应用重启后会重新推送。因为retry是消费端内部处理的，包括异常也是内部处理，对于rabbitmq是不知道的（此场景解决方案后面有）
 *   6、spring.rabbitmq.listener.retry配置的重发是在消费端应用内处理的，不是rabbitqq重发
 */
@Component
public class MQConsumer {

    @RabbitHandler
    @RabbitListener(queues = MQProperties.A_QUEUE)
    public void receiverA(Message msg, Channel channel) throws IOException {
        System.out.println("消费者broad-A：-》" + new String(msg.getBody()));
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitHandler
    @RabbitListener(queues = MQProperties.B_QUEUE)
    public void receiverB(Message msg, Channel channel) throws IOException {
        System.out.println("消费者-broadB：-》" + new String(msg.getBody()));
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitHandler
    @RabbitListener(queues = MQProperties.C_QUEUE)
    public void receiverC(Message msg, Channel channel) throws IOException {
        try {
            String s = new String(msg.getBody());
            System.out.println("消费者route-C：-》" + s);
            int i = 1/0;
            // 拒绝确认会重新放任队列
            // channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } catch (Exception e) {
            // 在数据库保存失败消息
            // todo

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
//            e.printStackTrace();
        }
    }

    @RabbitHandler
    @RabbitListener(queues = MQProperties.D_QUEUE)
    public void receiverD(Message msg, Channel channel) throws IOException {
        System.out.println("消费者route-D：-》" + new String(msg.getBody()));
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitHandler
    @RabbitListener(queues = MQProperties.message)
    public void receiverM(Message msg, Channel channel) throws IOException {
        System.out.println("消费者topic-M：-》" + new String(msg.getBody()));
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitHandler
    @RabbitListener(queues = MQProperties.messages)
    public void receiverMs(Message msg, Channel channel) throws IOException {
        System.out.println("消费者topic-Ms：-》" + new String(msg.getBody()));
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }
}
