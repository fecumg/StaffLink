package fpt.edu.fileservice.configurations;

import org.springframework.amqp.core.*;
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

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Value("${rabbitmq.queue.avatar-processing}")
    private String AVATAR_PROCESSING_QUEUE_NAME;

    @Value("${rabbitmq.queue.attachment-processing}")
    private String ATTACHMENT_PROCESSING_QUEUE_NAME;

    @Value("${rabbitmq.routing-key.attachment-processing-done}")
    private String ATTACHMENT_PROCESSING_DONE_ROUTING_KEY;
    @Value("${rabbitmq.queue.attachment-processing-done}")
    private String ATTACHMENT_PROCESSING_DONE_QUEUE_NAME;

    @Value("${rabbitmq.queue.attachment-removal}")
    private String ATTACHMENT_REMOVAL_QUEUE_NAME;

    @Bean
    Queue avatarProcessingQueue() {
        return new Queue(AVATAR_PROCESSING_QUEUE_NAME, false);
    }

    @Bean
    Queue attachmentProcessingQueue() {
        return new Queue(ATTACHMENT_PROCESSING_QUEUE_NAME, false);
    }


    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue attachmentDoneQueue() {
        return new Queue(ATTACHMENT_PROCESSING_DONE_QUEUE_NAME, false);
    }
    @Bean
    Binding attachmentDoneBinding(Queue attachmentDoneQueue, TopicExchange exchange) {
        return BindingBuilder.bind(attachmentDoneQueue).to(exchange).with(ATTACHMENT_PROCESSING_DONE_ROUTING_KEY);
    }


    @Bean
    Queue attachmentRemovalQueue() {
        return new Queue(ATTACHMENT_REMOVAL_QUEUE_NAME, false);
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
