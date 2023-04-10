package fpt.edu.taskservice.configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Truong Duc Duong
 */

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Value("${rabbitmq.queue.attachment-processing}")
    private String ATTACHMENT_PROCESSING_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.attachment-processing}")
    private String ATTACHMENT_PROCESSING_ROUTING_KEY;

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    @Bean
    Queue attachmentQueue() {
        return new Queue(ATTACHMENT_PROCESSING_QUEUE_NAME, false);
    }

    @Bean
    Binding attachmentBinding(Queue attachmentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(attachmentQueue).to(exchange).with(ATTACHMENT_PROCESSING_ROUTING_KEY);
    }


    @Bean
    public MessageConverter jsonConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter jsonConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonConverter);
        return rabbitTemplate;
    }
}
