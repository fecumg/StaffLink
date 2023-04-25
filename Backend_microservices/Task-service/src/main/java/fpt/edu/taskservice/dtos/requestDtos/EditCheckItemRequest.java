package fpt.edu.taskservice.dtos.requestDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditCheckItemRequest {
    private boolean checked;
}
