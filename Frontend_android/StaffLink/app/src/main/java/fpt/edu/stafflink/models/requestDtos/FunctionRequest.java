package fpt.edu.stafflink.models.requestDtos;

/**
 * @author Truong Duc Duong
 */

public class FunctionRequest {

    private String name;

    private String description;

    private String uri;

    private int parentId;

    private boolean displayed;

    public FunctionRequest(String name, String description, String uri, int parentId, boolean displayed) {
        this.name = name;
        this.description = description;
        this.uri = uri;
        this.parentId = parentId;
        this.displayed = displayed;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }
}
