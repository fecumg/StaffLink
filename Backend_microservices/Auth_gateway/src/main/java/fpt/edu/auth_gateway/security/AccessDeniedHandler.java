package fpt.edu.auth_gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.auth_gateway.responses.ErrorApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Component
@Log4j2
public class AccessDeniedHandler implements ServerAccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        log.error(denied.getMessage());
        Mono<DataBuffer> dataBufferMono = Mono.just(denied).map(exception -> {
            ErrorApiResponse errorApiResponse;
            try {
                errorApiResponse = new ErrorApiResponse(exception.getMessage());
                return objectMapper.writeValueAsBytes(errorApiResponse);
            } catch (JsonProcessingException e) {
                log.error("Error occurred during processing JSON: " + e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }).map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes));

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(dataBufferMono);
    }
}
