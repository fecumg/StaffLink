package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.utilities.GenericUtils;

/**
 * @author Truong Duc Duong
 */

public class UserResponse extends BaseResponse {
    private int id;
    private String name, username, address, phone, email, avatar;
    private List<RoleResponse> roles;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleResponse> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof UserResponse) {
            return this.id != 0 && ((UserResponse) obj).getId() == this.id;
        }
        return false;
    }
}
