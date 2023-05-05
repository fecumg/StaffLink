package fpt.edu.stafflink.models.others;

import java.util.stream.Collectors;

import fpt.edu.stafflink.models.responseDtos.UserResponse;

public class SearchedUser {
    private int id;
    private String name;
    private String roles;

    public SearchedUser(UserResponse userResponse) {
        this.id = userResponse.getId();
        this.name = userResponse.getName();
        if (userResponse.getRoles() != null) {
            roles = userResponse.getRoles().stream()
                    .map(roleResponse -> roleResponse.getName())
                    .collect(Collectors.joining(", "));
        } else {
            this.roles = "";
        }
    }

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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
