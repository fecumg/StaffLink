package fpt.edu.user_service.dtos.requestDtos.userRequestDtos;


import fpt.edu.user_service.customAnnotations.FieldsMatch;
import fpt.edu.user_service.customAnnotations.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldsMatch(field = "password", matchedField = "confirmPassword")
public class NewUserRequest {

    @NotNull(message = "Name cannot be empty")
    @Size(max = 50, message = "Name cannot exceed {max} characters")
    private String name;

    @NotNull(message = "Username cannot be empty")
    @Size(min = 6, max = 50, message = "Username cannot be less than {min} nor exceed {max} characters")
    private String username;

    @NotNull(message = "Address cannot be empty")
    @Size(max = 50, message = "Address cannot exceed {max} characters")
    private String address;

    @NotNull(message = "Phone cannot be empty")
    @Phone
    private String phone;

    @NotNull(message = "Email cannot be empty")
    @Email
    private String email;

    @NotNull(message = "Password cannot be empty")
    @Size(min = 6, message = "Password cannot be less than {min} characters")
    @Schema(format = "password")
    private String password;

    @NotNull(message = "Password cannot be empty")
    @Size(min = 6, message = "Password cannot be less than {min} characters")
    @Schema(format = "password")
    private String confirmPassword;

    @Nullable
    private MultipartFile avatar;

    private List<Integer> roleIds;
}
