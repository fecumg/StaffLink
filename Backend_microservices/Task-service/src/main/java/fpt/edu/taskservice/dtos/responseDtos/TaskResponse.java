package fpt.edu.taskservice.dtos.responseDtos;

import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private Date dueAt;
    private List<Integer> userIds;
    private String status;
    private int statusCode;
    private List<AttachmentResponse> attachments;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.dueAt = task.getDueAt();
        this.userIds = task.getUserIds();

        super.setCreatedAt(task.getCreatedAt());
        super.setCreatedBy(task.getCreatedBy());
        super.setUpdatedAt(task.getUpdatedAt());
        super.setUpdatedBy(task.getUpdatedBy());

        this.status = TaskStatus.getMessage(task.getStatus());
        this.statusCode = task.getStatus();
    }
}