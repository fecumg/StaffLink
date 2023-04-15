package fpt.edu.stafflink.enums;

import java.util.Arrays;

/**
 * @author Truong Duc Duong
 */

public enum TaskStatus {
    INITIATED(0, "initiated"),
    IN_PROGRESS(1, "in progress"),
    PENDING(2, "pending"),
    COMPLETED(3, "completed"),
    OVERDUE(4, "overdue"),
    FAILED(5, "failed");

    TaskStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getMessage(int code) {
        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.getCode() == code)
                .findFirst()
                .map(TaskStatus::getMessage)
                .orElse(FAILED.getMessage());
    }

    public static int getCode(String message) {
        return Arrays.stream(TaskStatus.values())
                .filter(status -> message.equals(status.getMessage()))
                .findFirst()
                .map(TaskStatus::getCode)
                .orElse(FAILED.getCode());
    }

    public static TaskStatus getTaskStatusFormCode(int code) {
        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.getCode() == code)
                .findFirst()
                .orElse(TaskStatus.INITIATED);
    }


}
