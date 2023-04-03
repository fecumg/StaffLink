package fpt.edu.stafflink.models.requestDtos.userRequestDtos;

/**
 * @author Truong Duc Duong
 */

public class LoginRequest {

    private String username, password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
