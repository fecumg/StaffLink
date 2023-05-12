package fpt.edu.stafflink.models.others;

import fpt.edu.stafflink.models.responseDtos.CommentResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;

public class DisplayedComment {
    private CommentResponse comment;
    private UserResponse user;

    public DisplayedComment(CommentResponse comment) {
        this.comment = comment;
    }

    public DisplayedComment(CommentResponse comment, UserResponse user) {
        this.comment = comment;
        this.user = user;
    }

    public CommentResponse getComment() {
        return comment;
    }

    public void setComment(CommentResponse comment) {
        this.comment = comment;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
