package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */


public class AttachmentResponse {
    private String id;
    private String name;
    private Date createdAt;
    private int createdBy;

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
        if (obj instanceof AttachmentResponse) {
            return StringUtils.isNotEmpty(this.id) && ((AttachmentResponse) obj).getId().equals(this.id);
        }
        return false;
    }
}
