package fpt.edu.user_service.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorApiResponse {
    private String message;
    private List<String> details;
    private Object data;

    public ErrorApiResponse(String message) {
        this.message = message;
    }

    public ErrorApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ErrorApiResponse(String message, List<String> details) {
        this.message = message;
        this.details = details;
    }
}
