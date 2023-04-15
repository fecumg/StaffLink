package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ProjectResponse extends BaseResponse{
    private String id;
    private String name;
    private String description;
    private List<Integer> userIds;
    private List<TaskResponse> tasks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<TaskResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.tasks = tasks;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ProjectResponse) {
            return StringUtils.isNotEmpty(this.id) && ((ProjectResponse) obj).getId().equals(this.id);
        }
        return false;
    }
}
