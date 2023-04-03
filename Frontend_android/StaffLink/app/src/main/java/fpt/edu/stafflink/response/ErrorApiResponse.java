package fpt.edu.stafflink.response;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

public class ErrorApiResponse {
    private String message;
    private List<String> details;
    private Object data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
