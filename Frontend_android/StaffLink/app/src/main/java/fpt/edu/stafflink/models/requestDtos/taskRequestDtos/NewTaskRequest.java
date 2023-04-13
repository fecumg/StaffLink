package fpt.edu.stafflink.models.requestDtos.taskRequestDtos;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class NewTaskRequest {
    private String name;
    private String description;
    private String dueDate;
    private List<Integer> userIds;
    private String projectId;

    public NewTaskRequest(String name, String description, String dueDate, List<Integer> userIds, String projectId) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.userIds = userIds;
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
