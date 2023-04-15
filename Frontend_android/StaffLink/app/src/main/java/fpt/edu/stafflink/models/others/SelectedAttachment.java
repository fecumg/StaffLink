package fpt.edu.stafflink.models.others;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class SelectedAttachment {

    private String id;
    private String name;
    private String taskId;

    public SelectedAttachment(String id, String name, String taskId) {
        this.id = id;
        this.name = name;
        this.taskId = taskId;
    }

    public SelectedAttachment(String name, String taskId) {
        this.name = name;
        this.taskId = taskId;
    }

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

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SelectedAttachment) {
            return StringUtils.isNotEmpty(this.id) && ((SelectedAttachment) obj).getId().equals(this.id);
        }
        return false;
    }
}
