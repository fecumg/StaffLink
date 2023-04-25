package fpt.edu.taskservice.dtos.responseDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fpt.edu.taskservice.entities.Attachment;
import lombok.*;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private String id;
    private String name;

    @JsonSerialize(as = Date.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    private Date createdAt;
    private int createdBy;

    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.name = attachment.getName();
        this.createdAt = attachment.getCreatedAt();
        this.createdBy = attachment.getCreatedBy();
    }
}
