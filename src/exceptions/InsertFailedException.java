package exceptions;

public class InsertFailedException extends ServerException {
    public InsertFailedException(String message) {
        super(message);
    }
}