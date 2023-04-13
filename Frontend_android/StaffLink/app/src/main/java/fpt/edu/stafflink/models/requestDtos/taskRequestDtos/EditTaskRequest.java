package fpt.edu.stafflink.models.requestDtos.taskRequestDtos;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class EditTaskRequest {
    private String name;
    private String description;
    private String dueDate;
    private int status;
    private List<Integer> userIds;

    public EditTaskRequest(String name, String description, String dueDate, int status, List<Integer> userIds) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.userIds = userIds;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
