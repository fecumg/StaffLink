package fpt.edu.user_service.dtos.requestDtos;

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
public class FunctionRequest {

    @NotNull(message = "Name cannot be empty")
    @Size(max = 50, message = "Name cannot exceed {max} characters")
    private String name;

    @NotNull(message = "Description cannot be empty")
    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    @Size(max = 500, message = "Uri cannot exceed {max} characters")
    private String uri;

    private int parentId;

    private boolean displayed = true;
}
