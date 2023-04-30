package fpt.edu.taskservice.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticResponse {
    private long initiatedTaskAmount;
    private long inProgressTaskAmount;
    private long overdueTaskAmount;
}
