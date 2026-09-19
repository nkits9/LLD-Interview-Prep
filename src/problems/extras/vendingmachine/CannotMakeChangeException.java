package problems.extras.vendingmachine;

/** The machine refuses the sale rather than short-change the customer. */
public class CannotMakeChangeException extends RuntimeException {
    public CannotMakeChangeException(long changePaise) {
        super("cannot return " + changePaise + "p in change — sale refused, coins refunded");
    }
}
