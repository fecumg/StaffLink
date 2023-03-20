package fpt.edu.auth_gateway.security.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.auth_gateway.responses.ErrorApiResponse;
import fpt.edu.auth_gateway.security.authObjects.AuthenticatedUser;
import fpt.edu.auth_gateway.security.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @author Truong Duc Duong
 */

@Component
@Log4j2
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    @Autowired
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String excludedPaths;
        private boolean guardByDefault = true;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestPath = exchange.getRequest().getURI().getPath();
            if (config.isGuardByDefault()) {
                return this.isFreePath(config, exchange)
                        .filter(bool -> bool)
                        .doOnNext(bool ->
                                log.info("Free access to '{}', declared in application.properties", requestPath))
                        .flatMap(bool -> chain.filter(exchange))
                        .switchIfEmpty(this.authorize(exchange, chain));
            } else {
                return authService.isGuardedPath(exchange)
                        .filter(bool -> bool)
                        .flatMap(bool -> this.authorize(exchange, chain))
                        .switchIfEmpty(chain.filter(exchange));
            }
        };
    }

    private Mono<Void> authorize(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(authentication -> (AuthenticatedUser) authentication.getPrincipal())
                .filter(authenticatedUser -> !authService.isAuthorized(exchange, authenticatedUser))
                .map(authenticatedUser -> {
                        log.warn("Access of user '{}' to path '{}' has been denied", authenticatedUser.getUsername(), exchange.getRequest().getURI().getPath());
                        return forceResponse(exchange);
                })
                .flatMap(voidMono -> voidMono)
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Boolean> isFreePath(Config config, ServerWebExchange exchange) {
        PathMatcher pathMatcher = new AntPathMatcher();
        return Mono.just(config.getExcludedPaths())
                .filter(StringUtils::hasText)
                .map(excludedPathsString -> Arrays.stream(excludedPathsString.split(", ")).toList())
                .flatMapMany(Flux::fromIterable)
                .any(freePath ->
                        pathMatcher.match(freePath, exchange.getRequest().getURI().getPath()))
                .defaultIfEmpty(false);
    }

    private Mono<Void> forceResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorApiResponse errorApiResponse;
        DataBuffer dataBuffer;
        byte[] bytes;
        try {
            errorApiResponse = new ErrorApiResponse("Unauthorized");
            bytes = objectMapper.writeValueAsBytes(errorApiResponse);
            dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        } catch (JsonProcessingException e) {
            log.error("Error occurred during processing JSON: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
