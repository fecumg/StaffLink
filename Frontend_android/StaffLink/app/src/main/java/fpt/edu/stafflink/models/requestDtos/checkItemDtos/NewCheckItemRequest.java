package fpt.edu.stafflink.models.requestDtos.checkItemDtos;

import android.net.Uri;

public class NewCheckItemRequest {
    private String content;
    private String taskId;

    public NewCheckItemRequest(String content, String taskId) {
        this.content = content;
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}