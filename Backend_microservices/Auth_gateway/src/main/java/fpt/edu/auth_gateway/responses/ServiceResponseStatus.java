package fpt.edu.auth_gateway.responses;

/**
 * @author Truong Duc Duong
 */

@Deprecated
public enum ServiceResponseStatus {
    SUCCESS(0, "Success"),
    SYSTEM_ERROR(1, "Error"),
    NOT_FOUND(2, "Data not found"),
    BAD_REQUEST(3, "Bad request");
    public int code;
    public String message;

    ServiceResponseStatus(int code , String message){
        this.code = code;
        this.message = message;
    }
}
