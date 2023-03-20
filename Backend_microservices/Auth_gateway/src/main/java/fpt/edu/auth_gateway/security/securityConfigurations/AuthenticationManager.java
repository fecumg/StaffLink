package fpt.edu.auth_gateway.security.securityConfigurations;

import fpt.edu.auth_gateway.security.services.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Component
@Log4j2
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private AuthService authService;
    @Autowired
    private AnonymousBuilder anonymousBuilder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return authService.loadUserByToken(authentication.getPrincipal().toString())
                .map(authenticatedUser -> (Authentication) new UsernamePasswordAuthenticationToken(
                                    authenticatedUser,
                                    null,
                                    authenticatedUser.getAuthorities()))
                .switchIfEmpty(Mono.just(anonymousBuilder.build()));
    }
}
