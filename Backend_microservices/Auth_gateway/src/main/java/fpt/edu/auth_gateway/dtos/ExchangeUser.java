package fpt.edu.auth_gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
