package fpt.edu.taskservice.configurations.webSocket;

import fpt.edu.taskservice.entities.Comment;
import fpt.edu.taskservice.services.CommentService;
import fpt.edu.taskservice.services.impls.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Component
public class WebFluxWebSocketHandler extends BaseService<Comment> implements WebSocketHandler {

    @Autowired
    private CommentService commentService;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        Flux<WebSocketMessage> webSocketMessageFlux = webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> commentService.save(payload, webSocketSession))
                .map(webSocketSession::textMessage);
        return webSocketSession.send(webSocketMessageFlux);
    }
}
