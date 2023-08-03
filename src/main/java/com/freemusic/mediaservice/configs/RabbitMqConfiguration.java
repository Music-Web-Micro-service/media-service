package com.freemusic.mediaservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

        @Bean
        public Queue myQueue() {
            return new Queue("MusicMediaQueue", true);
        }

        @Bean
        public DirectExchange directExchange() {
            return new DirectExchange("MusicExchanges");
        }

        @Bean
        public Binding binding(Queue queue, DirectExchange directExchange) {
            return BindingBuilder.bind(queue).to(directExchange).with("MusicMedia");
        }

        @Bean
        public MessageConverter jsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
        }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, @Autowired MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}