package cnytez.reddit.app.exception;

public class ImageServerFailureException extends RuntimeException {
    public ImageServerFailureException(String message) {
        super(message);
    }
}
