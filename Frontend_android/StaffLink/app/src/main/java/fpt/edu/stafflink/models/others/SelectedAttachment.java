package fpt.edu.stafflink.models.others;

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
}
