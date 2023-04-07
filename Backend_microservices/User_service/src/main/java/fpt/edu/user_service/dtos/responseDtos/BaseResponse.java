package fpt.edu.user_service.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.Date;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class BaseResponse {

    private Date createdAt;
    private int createdBy;


    private Date updatedAt;
    private int updatedBy;
}
