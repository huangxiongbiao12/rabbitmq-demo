package com.example.demo.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqRouteConfig {

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("directExchange");//配置广播路由器
    }

    @Bean
    Binding bindingExchangeC(@Qualifier("cQueue") Queue CMessage, DirectExchange directExchange) {
        return BindingBuilder.bind(CMessage).to(directExchange).with("c");
    }

    @Bean
    Binding bindingExchangeCC(@Qualifier("cQueue") Queue CMessage, DirectExchange directExchange) {
        return BindingBuilder.bind(CMessage).to(directExchange).with("cc");
    }

    @Bean
    Binding bindingExchangeD(@Qualifier("dQueue") Queue DMessage, DirectExchange directExchange) {
        return BindingBuilder.bind(DMessage).to(directExchange).with("d");
    }

}
