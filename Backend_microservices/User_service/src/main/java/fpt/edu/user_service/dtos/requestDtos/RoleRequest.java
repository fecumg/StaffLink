package fpt.edu.user_service.dtos.requestDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class RoleRequest {

    @NotNull(message = "Name cannot be empty")
    @Size(max = 50, message = "Name cannot exceed {max} characters")
    private String name;

    @NotNull(message = "Description cannot be empty")
    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    private List<Integer> functionIds;
}
