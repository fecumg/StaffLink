package fpt.edu.stafflink.models.requestDtos;

import android.net.Uri;

/**
 * @author Truong Duc Duong
 */

public class AttachmentRequest {
    private Uri uri;
    private String taskId;

    public AttachmentRequest(Uri uri, String taskId) {
        this.uri = uri;
        this.taskId = taskId;
    }

    public Uri getUri() {
        return uri;
    }

    public void setAttachment(Uri uri) {
        this.uri = uri;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
