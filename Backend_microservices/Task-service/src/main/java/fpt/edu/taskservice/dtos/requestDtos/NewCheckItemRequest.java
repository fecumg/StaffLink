package fpt.edu.taskservice.dtos.requestDtos;

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
public class NewCheckItemRequest {

    @NotNull(message = "Content cannot be empty")
    @Size(max = 200, message = "Content cannot exceed {max} characters")
    private String content;
    private String taskId;
}
