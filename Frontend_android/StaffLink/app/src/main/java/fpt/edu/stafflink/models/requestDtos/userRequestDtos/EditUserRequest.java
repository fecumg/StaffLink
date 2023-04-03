package fpt.edu.stafflink.models.requestDtos.userRequestDtos;

import java.io.File;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class EditUserRequest {
    private String name;

    private String username;

    private String address;

    private String phone;

    private String email;

    private File avatar;

    private List<Integer> roleIds;

    public EditUserRequest(String name, String username, String address, String phone, String email, File avatar) {
        this.name = name;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
    }

    public EditUserRequest(String name, String username, String address, String phone, String email, File avatar, List<Integer> roleIds) {
        this.name = name;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.roleIds = roleIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public File getAvatar() {
        return avatar;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    public List<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Integer> roleIds) {
        this.roleIds = roleIds;
    }
}
