package fpt.edu.stafflink.models.requestDtos;

/**
 * @author Truong Duc Duong
 */

public class ProjectRequest {
    private String name;
    private String description;

    public ProjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}