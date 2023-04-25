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

@Document(collection = "comments")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    private String id;
    private String content;

    @DocumentReference(lazy = true)
    private Task task;

    @CreatedDate
    private Date createdAt;

    private int createdBy;

    public Comment(String content, Task task) {
        this.content = content;
        this.task = task;
    }
}
