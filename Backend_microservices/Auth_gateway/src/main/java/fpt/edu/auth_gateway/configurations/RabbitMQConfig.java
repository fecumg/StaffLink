package fpt.edu.auth_gateway.configurations;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
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

    @Value("${rabbitmq.queue.new-auth-cache}")
    private String NEW_AUTH_CACHE_QUEUE_NAME;
    @Value("${rabbitmq.queue.auth-cache-update}")
    private String AUTH_CACHE_UPDATE_QUEUE_NAME;
    @Value("${rabbitmq.queue.auth-cache-delete}")
    private String AUTH_CACHE_DELETE_QUEUE_NAME;
    @Value("${rabbitmq.queue.guarded-path-update}")
    private String GUARDED_PATH_UPDATE_QUEUE_NAME;
    @Value("${rabbitmq.queue.guarded-path-delete}")
    private String GUARDED_PATH_DELETE_QUEUE_NAME;

    @Bean
    Queue newAuthCacheQueue() {
        return new Queue(NEW_AUTH_CACHE_QUEUE_NAME, false);
    }

    @Bean
    Queue authCacheUpdateQueue() {
        return new Queue(AUTH_CACHE_UPDATE_QUEUE_NAME, false);
    }

    @Bean
    Queue authCacheDeleteQueue() {
        return new Queue(AUTH_CACHE_DELETE_QUEUE_NAME, false);
    }

    @Bean
    Queue pathsCacheUpdateQueue() {
        return new Queue(GUARDED_PATH_UPDATE_QUEUE_NAME, false);
    }

    @Bean
    Queue pathsCacheDeleteQueue() {
        return new Queue(GUARDED_PATH_DELETE_QUEUE_NAME, false);
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
