package fpt.edu.user_service.dtos.authenticationDtos;

import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.entities.Role;
import fpt.edu.user_service.entities.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeUser {
    private int id;
    private String username;
    private String email;
    private String password;
    private List<String> roles;
    private List<String> authorizedUris;

    public static ExchangeUser build(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> authorizedUris = loadAuthorizedUris(user);

        return new ExchangeUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                roles,
                authorizedUris
        );
    }

    private static List<String> loadAuthorizedUris(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getFunctions().stream())
                .map(Function::getUri)
                .distinct()
                .toList();
    }
}
