package fpt.edu.taskservice.entities;

import fpt.edu.taskservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Document(collection = "tasks")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {

    private String name;

    private String description;

    private Date dueAt;

    @DocumentReference(lazy = true)
    private Project project;

    protected List<Integer> userIds;

    private int createdBy;

    private int updatedBy;

    private int status = TaskStatus.INITIATED.getCode();
}