package patterns.behavioral.state;

/** Specific exception over a generic one — illegal transitions are rejected explicitly. */
public class IllegalStateTransitionException extends IllegalStateException {
    public IllegalStateTransitionException(String state, String action) {
        super("cannot " + action + " an order in state " + state);
    }
}
