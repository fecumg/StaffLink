package fpt.edu.user_service.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class BaseResponse implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private Date createdAt;
    private UserResponse createdBy;

    private Date updatedAt;
    private UserResponse updatedBy;
}
