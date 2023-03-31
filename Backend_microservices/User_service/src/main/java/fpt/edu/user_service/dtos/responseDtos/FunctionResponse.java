package fpt.edu.user_service.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Truong Duc Duong
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse extends BaseResponse {
    private int id;
    private String name, description, uri;

    private FunctionResponse parent;

    private boolean displayed;
}
