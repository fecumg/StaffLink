package fpt.edu.auth_gateway.errorHandling;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * @author Truong Duc Duong
 */

@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        Map<String, Object> map = super.getErrorAttributes(request, options);

        if (error instanceof WebClientResponseException.BadRequest) {
            map.put("status", HttpStatus.BAD_REQUEST.value());
            map.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        } else if (error instanceof WebClientResponseException.Unauthorized) {
            map.put("status", HttpStatus.UNAUTHORIZED.value());
            map.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        } else {
            map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }

        map.put("message", error.getMessage());
        map.put("details", null);
        map.put("data", null);
        map.remove("trace");
        return map;
    }
}
