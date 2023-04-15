package fpt.edu.stafflink.models.responseDtos;

import androidx.annotation.Nullable;

import fpt.edu.stafflink.utilities.GenericUtils;

/**
 * @author Truong Duc Duong
 */

public class FunctionResponse extends BaseResponse {
    private int id;
    private String name, description, uri;
    private boolean displayed;

    private FunctionResponse parent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public FunctionResponse getParent() {
        return parent;
    }

    public void setParent(FunctionResponse parent) {
        this.parent = parent;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof FunctionResponse) {
            return this.id != 0 && ((FunctionResponse) obj).getId() == this.id;
        }
        return false;
    }
}
