package fpt.edu.user_service.configurations;

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

    @Value("${rabbitmq.queue.new-auth-cache}")
    private String NEW_AUTH_CACHE_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.new-auth-cache}")
    private String NEW_AUTH_CACHE_ROUTING_KEY;

    @Value("${rabbitmq.queue.auth-cache-update}")
    private String AUTH_CACHE_UPDATE_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.auth-cache-update}")
    private String AUTH_CACHE_UPDATE_ROUTING_KEY;

    @Value("${rabbitmq.queue.auth-cache-delete}")
    private String AUTH_CACHE_DELETE_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.auth-cache-delete}")
    private String AUTH_CACHE_DELETE_ROUTING_KEY;

    @Value("${rabbitmq.queue.guarded-path-update}")
    private String GUARDED_PATH_UPDATE_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.guarded-path-update}")
    private String GUARDED_PATH_UPDATE_ROUTING_KEY;

    @Value("${rabbitmq.queue.guarded-path-delete}")
    private String GUARDED_PATH_DELETE_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.guarded-path-delete}")
    private String GUARDED_PATH_DELETE_ROUTING_KEY;

    @Value("${rabbitmq.queue.avatar-processing}")
    private String AVATAR_PROCESSING_QUEUE_NAME;
    @Value("${rabbitmq.routing-key.avatar-processing}")
    private String AVATAR_PROCESSING_ROUTING_KEY;

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    @Bean
    Queue newAuthCacheQueue() {
        return new Queue(NEW_AUTH_CACHE_QUEUE_NAME, false);
    }

    @Bean
    Binding newAuthCacheBinding(Queue newAuthCacheQueue, TopicExchange exchange) {
        return BindingBuilder.bind(newAuthCacheQueue).to(exchange).with(NEW_AUTH_CACHE_ROUTING_KEY);
    }


    @Bean
    Queue authCacheUpdateQueue() {
        return new Queue(AUTH_CACHE_UPDATE_QUEUE_NAME, false);
    }

    @Bean
    Binding authCacheUpdateBinding(Queue authCacheUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authCacheUpdateQueue).to(exchange).with(AUTH_CACHE_UPDATE_ROUTING_KEY);
    }


    @Bean
    Queue authCacheDeleteQueue() {
        return new Queue(AUTH_CACHE_DELETE_QUEUE_NAME, false);
    }

    @Bean
    Binding authCacheDeleteBinding(Queue authCacheDeleteQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authCacheDeleteQueue).to(exchange).with(AUTH_CACHE_DELETE_ROUTING_KEY);
    }


    @Bean
    Queue pathCacheUpdateQueue() {
        return new Queue(GUARDED_PATH_UPDATE_QUEUE_NAME, false);
    }

    @Bean
    Binding pathCacheUdateBinding(Queue pathCacheUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(pathCacheUpdateQueue).to(exchange).with(GUARDED_PATH_UPDATE_ROUTING_KEY);
    }


    @Bean
    Queue pathCacheDeleteQueue() {
        return new Queue(GUARDED_PATH_DELETE_QUEUE_NAME, false);
    }

    @Bean
    Binding pathCacheBinding(Queue pathCacheDeleteQueue, TopicExchange exchange) {
        return BindingBuilder.bind(pathCacheDeleteQueue).to(exchange).with(GUARDED_PATH_DELETE_ROUTING_KEY);
    }


    @Bean
    Queue fileProcessingQueue() {
        return new Queue(AVATAR_PROCESSING_QUEUE_NAME, false);
    }

    @Bean
    Binding fileProcessingBinding(Queue fileProcessingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(fileProcessingQueue).to(exchange).with(AVATAR_PROCESSING_ROUTING_KEY);
    }


    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter converter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}
