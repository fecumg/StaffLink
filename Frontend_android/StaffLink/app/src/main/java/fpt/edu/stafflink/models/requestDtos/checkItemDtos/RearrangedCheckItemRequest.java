package fpt.edu.stafflink.models.requestDtos.checkItemDtos;

public class RearrangedCheckItemRequest {
    private String id;
    private int position;

    public RearrangedCheckItemRequest(String id, int position) {
        this.id = id;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
