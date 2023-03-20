package fpt.edu.user_service.exceptions;

/**
 * @author Truong Duc Duong
 */
public class UniqueKeyViolationException extends Exception{
    public UniqueKeyViolationException() {
        super("Unique key violated");
    }

    public UniqueKeyViolationException(String message) {
        super(message);
    }
}
