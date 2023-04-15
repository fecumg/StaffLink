package fpt.edu.stafflink.models.others;

import androidx.annotation.Nullable;

import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.models.responseDtos.UserResponse;

public class TaskStatusDto {
    private int code;
    private String message;

    public TaskStatusDto(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public TaskStatusDto(TaskStatus taskStatus) {
        this.code = taskStatus.getCode();
        this.message = taskStatus.getMessage();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TaskStatusDto) {
            return this.code != 0 && ((TaskStatusDto) obj).getCode() == this.code;
        }
        return false;
    }
}
