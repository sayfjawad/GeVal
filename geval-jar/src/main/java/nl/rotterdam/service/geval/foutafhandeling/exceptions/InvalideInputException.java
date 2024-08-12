package nl.rotterdam.service.geval.foutafhandeling.exceptions;

public class InvalideInputException extends RuntimeException {
    public InvalideInputException(String message) {
        super(message);
    }

    public InvalideInputException(String message, Exception root) {
        super(message, root);
    }
}
