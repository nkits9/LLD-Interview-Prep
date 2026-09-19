package patterns.behavioral.chainofresponsibility;

/** A filter rejecting is an explicit, named outcome — not a null or a boolean. */
public class RequestRejectedException extends RuntimeException {
    public RequestRejectedException(String filter, String reason) {
        super(filter + ": " + reason);
    }
}
