package fpt.edu.taskservice.dtos.responseDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fpt.edu.taskservice.entities.CheckItem;
import lombok.*;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckItemResponse {
    private String id;
    private String content;

    private boolean isChecked;

    @JsonSerialize(as = Date.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    private Date createdAt;
    private int createdBy;

    public CheckItemResponse(CheckItem checkItem) {
        this.id = checkItem.getId();
        this.content = checkItem.getContent();
        this.isChecked = checkItem.isChecked();
        this.createdAt = checkItem.getCreatedAt();
        this.createdBy = checkItem.getCreatedBy();
    }
}
