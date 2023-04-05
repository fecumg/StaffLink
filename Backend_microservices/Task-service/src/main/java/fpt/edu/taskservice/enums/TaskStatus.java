package fpt.edu.taskservice.enums;

import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Truong Duc Duong
 */

@Getter
@AllArgsConstructor
public enum TaskStatus {
    INITIATED(0, "initiated"),
    IN_PROGRESS(1, "in progress"),
    PENDING(2, "pending"),
    COMPLETED(3, "completed"),
    OVERDUE(4, "overdue"),
    FAILED(5, "failed");

    private final int code;
    private final String message;

    public static String getMessage(int code) {
        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.getCode() == code)
                .findFirst()
                .map(TaskStatus::getMessage)
                .orElseThrow(() -> new BadRequestException("Status code invalid: " + code ));
    }
}
