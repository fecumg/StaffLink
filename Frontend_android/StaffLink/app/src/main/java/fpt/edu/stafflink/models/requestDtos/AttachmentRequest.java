package fpt.edu.stafflink.models.requestDtos;

import java.io.File;

/**
 * @author Truong Duc Duong
 */

public class AttachmentRequest {
    private File attachment;
    private String taskId;

    public AttachmentRequest(File attachment, String taskId) {
        this.attachment = attachment;
        this.taskId = taskId;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
