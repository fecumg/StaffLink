package fpt.edu.auth_gateway.security.filters;

import fpt.edu.auth_gateway.security.authObjects.AuthenticatedUser;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

/**
 * @author Truong Duc Duong
 */

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(authentication -> (AuthenticatedUser) authentication.getPrincipal())
                .map(authenticatedUser -> {
                    exchange.getRequest()
                            .mutate()
                            .header("x-auth-user-id", String.valueOf(authenticatedUser.getId()));
                    return exchange;
                })
                .flatMap(chain::filter)
                .switchIfEmpty(chain.filter(exchange)));
    }
}
