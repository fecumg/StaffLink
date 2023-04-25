package fpt.edu.taskservice.configurations.webSocket;

import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.repositories.TaskRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Truong Duc Duong
 */

@Configuration
@Log4j2
public class WebSocketConfig {
    @Autowired
    private WebSocketHandler webSocketHandler;
    @Autowired
    private TaskRepository taskRepository;

    @Bean
    public HandlerMapping handlerMapping(){
        Map<String, WebSocketHandler> urlMap = new HashMap<>();
        urlMap.put("/comments", webSocketHandler);

        List<Task> tasks = taskRepository.findAll()
                .collectList()
                .block();

        if (tasks != null) {
            tasks.forEach(task -> urlMap.put("/comments/" + task.getId(), webSocketHandler));
        }

        return new SimpleUrlHandlerMapping(urlMap);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}

