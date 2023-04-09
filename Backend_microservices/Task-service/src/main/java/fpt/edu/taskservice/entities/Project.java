package fpt.edu.taskservice.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Document(collection = "projects")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {
    private String name;
    private String description;

    @DocumentReference(lazy = true, lookup = "{ 'project' : ?#{#self._id} }")
    @ReadOnlyProperty
    private List<Task> tasks;

    protected List<Integer> userIds;
    private int createdBy;
    private int updatedBy;
}