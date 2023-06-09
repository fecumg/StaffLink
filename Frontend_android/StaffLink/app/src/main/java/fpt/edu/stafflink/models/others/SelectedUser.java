package fpt.edu.stafflink.models.others;

import androidx.annotation.Nullable;

public class SelectedUser {
    private int id;
    private String name;

    public SelectedUser(int id, String name) {
        this.id = id;
        this.name = name;
    }

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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SelectedUser) {
            return this.id != 0 && ((SelectedUser) obj).getId() == this.id;
        }
        return false;
    }
}
