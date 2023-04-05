package fpt.edu.taskservice.dtos.requestDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditTaskRequest {
    @NotNull(message = "Name cannot be empty")
    @Size(max = 50, message = "Name cannot exceed {max} characters")
    private String name;

    @NotNull(message = "Description cannot be empty")
    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    private String dueDate;

    private int status;

    private List<Integer> userIds;

    private List<FilePart> attachments;
}
