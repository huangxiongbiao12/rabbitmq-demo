package com.example.demo;

import com.example.demo.mq.MQProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MQSender implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    AtomicInteger i = new AtomicInteger(0);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(this);             //指定 ReturnCallback
        rabbitTemplate.setReturnCallback(this);
    }

    private int getNO() {
        return i.getAndIncrement();
    }

    @Scheduled(fixedRate = 1000)
    private void sendBroad() {
        String msg = "broad:" + getNO();
        CorrelationData correlationData = getCorrelationData("fanoutExchange", "", msg.getBytes());
        rabbitTemplate.convertAndSend("fanoutExchange", "", msg, correlationData);
//        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);
        System.out.println(msg);
    }

    @Scheduled(fixedRate = 1000)
    private void sendTopic() {
        String msg = "topic-m:" + getNO();
        String msgs = "topic-ms:" + getNO();
        CorrelationData correlationData = getCorrelationData("topicExchange", MQProperties.message, msg.getBytes());
        CorrelationData correlationDatas = getCorrelationData("topicExchange", MQProperties.message, msgs.getBytes());
        rabbitTemplate.convertAndSend("topicExchange", MQProperties.message, msg, correlationData);
        rabbitTemplate.convertAndSend("topicExchange", MQProperties.messages, msgs, correlationDatas);
        System.out.println(msg);
        System.out.println(msgs);
    }

    @Scheduled(fixedRate = 1000)
    private void sendRoute() {
        String msgc = "route-c:" + getNO();
        String msgcc = "route-cc:" + getNO();
        String msgd = "route-d:" + getNO();
        CorrelationData correlationData = getCorrelationData("directExchange", "c", msgc.getBytes());
        CorrelationData correlationDatacc = getCorrelationData("directExchange", "cc", msgcc.getBytes());
        CorrelationData correlationDatad = getCorrelationData("directExchange", "d", msgd.getBytes());
        rabbitTemplate.convertAndSend("directExchange", "c", msgc, correlationData);
        rabbitTemplate.convertAndSend("directExchange", "cc", msgcc, correlationDatacc);
        rabbitTemplate.convertAndSend("directExchange", "d", msgd, correlationDatad);
        System.out.println(msgc);
        System.out.println(msgcc);
        System.out.println(msgd);
    }

    /**
     * 生成相关重试对象信息
     *
     * @param exchange
     * @param routeKey
     * @param body
     * @return
     */
    private CorrelationData getCorrelationData(String exchange, String routeKey, byte[] body) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setReceivedExchange(exchange);
        messageProperties.setReceivedRoutingKey(routeKey);
        Message message = new Message(body, messageProperties);
        CorrelationData correlationData = new CorrelationData();
        correlationData.setReturnedMessage(message);
        return correlationData;
    }


    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("confirm消息发送成功：" + cause);
        } else {
            String msg = new String(correlationData.getReturnedMessage().getBody());
            System.out.println("confirm消息发送失败：" + msg);
            MessageProperties messageProperties = correlationData.getReturnedMessage().getMessageProperties();
            rabbitTemplate.convertAndSend(messageProperties.getReceivedExchange(),
                    messageProperties.getReceivedRoutingKey(),
                    correlationData.getReturnedMessage(),
                    correlationData);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String msg = new String(message.getBody());
        System.out.println("returnedMessage消息发送失败：" + msg);
        // 到达队列失败重新发送
        CorrelationData correlationData = getCorrelationData(exchange, routingKey, message.getBody());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }
}
