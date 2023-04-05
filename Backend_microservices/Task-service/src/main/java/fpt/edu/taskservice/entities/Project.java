package fpt.edu.taskservice.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private int createdBy;

    private int updatedBy;
}