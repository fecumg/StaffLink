package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class CommentResponse {
    private String id;
    private String content;
    private Date createdAt;
    private int createdBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof CommentResponse) {
            return StringUtils.isNotEmpty(this.id) && ((CommentResponse) obj).getId().equals(this.id);
        }
        return false;
    }
}
