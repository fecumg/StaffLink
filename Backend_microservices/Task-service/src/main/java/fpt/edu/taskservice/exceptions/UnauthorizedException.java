package fpt.edu.taskservice.exceptions;

/**
 * @author Truong Duc Duong
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
