package fpt.edu.stafflink.models.requestDtos.checkItemDtos;

public class EditCheckItemRequest {

    private boolean checked;

    public EditCheckItemRequest(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
