package fpt.edu.auth_gateway.security.services;

import fpt.edu.auth_gateway.security.authObjects.AuthenticatedUser;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */
public interface AuthService {
    Mono<AuthenticatedUser> loadUserByToken(String token);
    boolean isAuthorized(ServerWebExchange exchange, AuthenticatedUser authenticatedUser);
    Mono<Boolean> isGuardedPath(ServerWebExchange exchange);
}
