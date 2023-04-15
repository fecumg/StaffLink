package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import fpt.edu.stafflink.utilities.GenericUtils;

/**
 * @author Truong Duc Duong
 */

public class TaskResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private Date dueAt;
    private List<Integer> userIds;
    private String status;
    private int statusCode;
    private List<AttachmentResponse> attachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<AttachmentResponse> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentResponse> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TaskResponse) {
            return StringUtils.isNotEmpty(this.id) && ((TaskResponse) obj).getId().equals(this.id);
        }
        return false;
    }
}