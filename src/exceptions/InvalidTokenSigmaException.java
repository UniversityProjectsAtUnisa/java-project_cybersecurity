package exceptions;

public class InvalidTokenSigmaException extends InvalidTokenException {

    private static final String message = "This sigma is not valid for this payload";

    public InvalidTokenSigmaException(String message) {
        super(message);
    }

    public InvalidTokenSigmaException() {
        super(message);
    }
}
