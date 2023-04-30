package fpt.edu.stafflink.models.responseDtos;

/**
 * @author Truong Duc Duong
 */

public class TaskStatisticResponse {
    private int initiatedTaskAmount;
    private int inProgressTaskAmount;
    private int overdueTaskAmount;

    public int getInitiatedTaskAmount() {
        return initiatedTaskAmount;
    }

    public void setInitiatedTaskAmount(int initiatedTaskAmount) {
        this.initiatedTaskAmount = initiatedTaskAmount;
    }

    public int getInProgressTaskAmount() {
        return inProgressTaskAmount;
    }

    public void setInProgressTaskAmount(int inProgressTaskAmount) {
        this.inProgressTaskAmount = inProgressTaskAmount;
    }

    public int getOverdueTaskAmount() {
        return overdueTaskAmount;
    }

    public void setOverdueTaskAmount(int overdueTaskAmount) {
        this.overdueTaskAmount = overdueTaskAmount;
    }
}
