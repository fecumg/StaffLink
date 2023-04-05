package fpt.edu.taskservice.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Document(collection = "attachments")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment{
    @Id
    private String id;

    private String name;

    @DocumentReference(lazy = true)
    private Task task;

    @CreatedDate
    private Date createdAt;

    private int createdBy;

    public Attachment(String name, Task task) {
        this.name = name;
        this.task = task;
    }
}
