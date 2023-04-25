package fpt.edu.stafflink.models.requestDtos.taskRequestDtos;

public class EditStatusRequest {
    private int status;

    public EditStatusRequest(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
