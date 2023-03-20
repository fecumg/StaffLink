package fpt.edu.auth_gateway.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotNull(message = "Username cannot be empty")
    @Size(min = 6, max = 50, message = "Username cannot be less than {min} or exceed {max} characters")
    private String username;

    @NotNull(message = "Password cannot be empty")
    @Size(min = 6, message = "Password cannot be less than {min} characters")
    private String password;
}
