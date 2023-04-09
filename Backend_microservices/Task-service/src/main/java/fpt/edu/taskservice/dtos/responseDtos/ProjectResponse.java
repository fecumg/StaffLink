package fpt.edu.taskservice.dtos.responseDtos;

import fpt.edu.taskservice.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse extends BaseResponse{
    private String id;
    private String name;
    private String description;
    protected List<Integer> userIds;
    private List<TaskResponse> tasks;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.userIds = project.getUserIds();

        super.setCreatedAt(project.getCreatedAt());
        super.setCreatedBy(project.getCreatedBy());
        super.setUpdatedAt(project.getUpdatedAt());
        super.setUpdatedBy(project.getUpdatedBy());
    }
}
