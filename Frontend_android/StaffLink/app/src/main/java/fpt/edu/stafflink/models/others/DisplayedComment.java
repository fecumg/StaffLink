package fpt.edu.stafflink.models.others;

import fpt.edu.stafflink.models.responseDtos.CommentResponse;

public class DisplayedComment {
    private CommentResponse comment;
    private String avatar;

    public DisplayedComment(CommentResponse comment) {
        this.comment = comment;
    }

    public DisplayedComment(CommentResponse comment, String avatar) {
        this.comment = comment;
        this.avatar = avatar;
    }

    public CommentResponse getComment() {
        return comment;
    }

    public void setComment(CommentResponse comment) {
        this.comment = comment;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
