package fpt.edu.user_service.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Truong Duc Duong
 */

@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class ServiceResponse implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private ServiceResponseStatus serviceResponseStatus;
    private String message;
    private Object data;

    public ServiceResponse(ServiceResponseStatus serviceResponseStatus) {
        this.serviceResponseStatus = serviceResponseStatus;
    }

    public ServiceResponse(ServiceResponseStatus serviceResponseStatus, String message) {
        this.serviceResponseStatus = serviceResponseStatus;
        this.message = message;
    }

    public ServiceResponse(ServiceResponseStatus serviceResponseStatus, Object data) {
        this.serviceResponseStatus = serviceResponseStatus;
        this.data = data;
    }
}
