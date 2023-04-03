package fpt.edu.stafflink.models.requestDtos;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class RoleRequest {

    private String name;

    private String description;

    private List<Integer> functionIds;

    public RoleRequest(String name, String description, List<Integer> functionIds) {
        this.name = name;
        this.description = description;
        this.functionIds = functionIds;
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

    public List<Integer> getFunctionIds() {
        return functionIds;
    }

    public void setFunctionIds(List<Integer> functionIds) {
        this.functionIds = functionIds;
    }
}
