package fpt.edu.stafflink.models.requestDtos.userRequestDtos;

import okhttp3.MultipartBody;

import java.io.File;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class NewUserRequest {

    private String name;

    private String username;

    private String address;

    private String phone;

    private String email;

    private String password;

    private String confirmPassword;

    private File avatar;

    private List<Integer> roleIds;

    public NewUserRequest(String name, String username, String address, String phone, String email, String password, String confirmPassword, File avatar, List<Integer> roleIds) {
        this.name = name;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
