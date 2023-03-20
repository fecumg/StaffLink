package fpt.edu.auth_gateway.security.securityConfigurations;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Component
public class AnonymousBuilder {

    public AnonymousAuthenticationToken build() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("anonymous", "anonymous", authorities);
        anonymous.setAuthenticated(false);

        return anonymous;
    }
}
