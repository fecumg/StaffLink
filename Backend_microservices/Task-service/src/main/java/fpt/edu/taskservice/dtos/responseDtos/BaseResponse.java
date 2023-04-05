package fpt.edu.taskservice.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.Date;

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