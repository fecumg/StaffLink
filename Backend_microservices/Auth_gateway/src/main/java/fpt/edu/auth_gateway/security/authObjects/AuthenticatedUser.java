package fpt.edu.auth_gateway.security.authObjects;

import fpt.edu.auth_gateway.dtos.ExchangeUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@RedisHash(value = "authenticatedUSer", timeToLive = 1)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser implements UserDetails {

    private int id;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private List<String> authorizedUris;

//    @TimeToLive
//    private Long timeToLive = 1L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public AuthenticatedUser(ExchangeUser exchangeUser) {
        id = exchangeUser.getId();
        username = exchangeUser.getUsername();
        email = exchangeUser.getEmail();
        password = exchangeUser.getPassword();
        authorities = exchangeUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        authorizedUris = exchangeUser.getAuthorizedUris();
    }
}
