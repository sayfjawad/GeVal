package nl.rotterdam.service.geval.foutafhandeling.exceptions;

public class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Exception root) {
        super(message, root);
    }
}
