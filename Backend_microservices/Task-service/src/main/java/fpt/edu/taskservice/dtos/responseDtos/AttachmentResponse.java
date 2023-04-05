package fpt.edu.taskservice.dtos.responseDtos;

import fpt.edu.taskservice.entities.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private String id;
    private String name;
    private Date createdAt;
    private int createdBy;

    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.name = attachment.getName();
        this.createdAt = attachment.getCreatedAt();
        this.createdBy = attachment.getCreatedBy();
    }
}
