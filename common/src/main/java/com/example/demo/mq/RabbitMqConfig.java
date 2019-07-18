package com.example.demo.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean(name = "aQueue")
    public Queue A_QUEUE() {
        return new Queue(MQProperties.A_QUEUE);
    }

    @Bean(name = "bQueue")
    public Queue B_QUEUE() {
        return new Queue(MQProperties.B_QUEUE);
    }

    @Bean(name = "cQueue")
    public Queue C_QUEUE() {
        return new Queue(MQProperties.C_QUEUE);
    }

    @Bean(name = "dQueue")
    public Queue D_QUEUE() {
        return new Queue(MQProperties.D_QUEUE);
    }

}
