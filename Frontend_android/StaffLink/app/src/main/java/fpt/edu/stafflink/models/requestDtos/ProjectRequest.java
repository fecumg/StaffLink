package fpt.edu.stafflink.models.requestDtos;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class ProjectRequest {
    private String name;
    private String description;
    private List<Integer> userIds;

    public ProjectRequest(String name, String description, List<Integer> userIds) {
        this.name = name;
        this.description = description;
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

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
