package fpt.edu.user_service.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse extends BaseResponse {
    private int id;
    private String name, description;
    private List<FunctionResponse> functions;
}
