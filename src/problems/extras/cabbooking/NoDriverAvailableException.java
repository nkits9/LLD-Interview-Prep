package problems.extras.cabbooking;

public class NoDriverAvailableException extends RuntimeException {
    public NoDriverAvailableException() {
        super("no driver available near the pickup");
    }
}
