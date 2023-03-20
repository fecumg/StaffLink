package fpt.edu.auth_gateway.security.securityConfigurations;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Component
@Log4j2
public class SecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AnonymousBuilder anonymousBuilder;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return null;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        SecurityContext anonymousSecurityContext = new SecurityContextImpl(anonymousBuilder.build());

        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .flatMap(authHeader -> {
                    String authToken = authHeader.substring(7);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);

                    return authenticationManager.authenticate(authentication)
                            .map(auth -> (SecurityContext) new SecurityContextImpl(auth));
                })
                .switchIfEmpty(Mono.just(anonymousSecurityContext));
    }
}
