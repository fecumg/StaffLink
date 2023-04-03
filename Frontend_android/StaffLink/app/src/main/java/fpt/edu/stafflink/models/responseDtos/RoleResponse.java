package fpt.edu.stafflink.models.responseDtos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class RoleResponse extends BaseResponse {
    private int id;
    private String name, description;
    private List<FunctionResponse> functions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<FunctionResponse> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionResponse> functions) {
        this.functions = functions;
    }
}
