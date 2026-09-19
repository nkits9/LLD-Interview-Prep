package patterns.behavioral.chainofresponsibility;

/**
 * Handler. Each link does ONE check (SRP) then forwards; rejecting throws,
 * which short-circuits the rest of the chain.
 */
public abstract class Filter {
    private Filter next;

    /** Returns the linked filter so chains read left-to-right. */
    public Filter linkWith(Filter next) {
        this.next = next;
        return next;
    }

    public void handle(Request request) {
        check(request); // throws RequestRejectedException to stop the chain
        if (next != null) {
            next.handle(request);
        }
    }

    protected abstract void check(Request request);
}
