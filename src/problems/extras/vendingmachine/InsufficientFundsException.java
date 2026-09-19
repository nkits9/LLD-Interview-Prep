package problems.extras.vendingmachine;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(long pricePaise, long insertedPaise) {
        super("price " + pricePaise + "p but only " + insertedPaise + "p inserted");
    }
}
