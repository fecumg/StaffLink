package fpt.edu.fileservice.configurations;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Truong Duc Duong
 */

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.avatar-processing}")
    private String AVATAR_PROCESSING_QUEUE_NAME;

    @Value("${rabbitmq.queue.attachment-processing}")
    private String ATTACHMENT_PROCESSING_QUEUE_NAME;

    @Bean
    Queue avatarProcessingQueue() {
        return new Queue(AVATAR_PROCESSING_QUEUE_NAME, false);
    }

    @Bean
    Queue attachmentProcessingQueue() {
        return new Queue(ATTACHMENT_PROCESSING_QUEUE_NAME, false);
    }


    @Bean
    public MessageConverter jsonConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MessageConverter simpleConverter() {
        return new SimpleMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        return new RabbitTemplate(connectionFactory);
    }
}
