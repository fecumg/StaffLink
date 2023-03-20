package fpt.edu.user_service.responses;

/**
 * @author Truong Duc Duong
 */
public enum ServiceResponseStatus {
    SUCCESS(0, "Success"),
    SYSTEM_ERROR(1, "Error"),
    NOT_FOUND(2, "Data not found"),
    BAD_REQUEST(3, "Bad request");
    public final int code;
    public final String message;

    ServiceResponseStatus(int code , String message){
        this.code = code;
        this.message = message;
    }
}
