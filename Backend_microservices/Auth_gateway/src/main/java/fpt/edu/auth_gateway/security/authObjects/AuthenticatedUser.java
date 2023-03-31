package fpt.edu.auth_gateway.security.authObjects;

import fpt.edu.auth_gateway.dtos.ExchangeUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

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
    private Collection<CustomGrantedAuthority> authorities;
    private List<String> authorizedUris;

//    @TimeToLive
//    private Long timeToLive = 1L;

    @Override
    public Collection<CustomGrantedAuthority> getAuthorities() {
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
                .map(CustomGrantedAuthority::new)
                .collect(Collectors.toList());
        authorizedUris = exchangeUser.getAuthorizedUris();
    }

    static class CustomGrantedAuthority implements GrantedAuthority {
        private String role;

        public CustomGrantedAuthority() {
        }

        public CustomGrantedAuthority(String role) {
            Assert.hasText(role, "A granted authority textual representation is required");
            this.role = role;
        }

        @Override
        public String getAuthority() {
            return this.role;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof CustomGrantedAuthority) {
                return this.role.equals(((CustomGrantedAuthority) obj).role);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.role.hashCode();
        }

        @Override
        public String toString() {
            return this.role;
        }
    }
}
