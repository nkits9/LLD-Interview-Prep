package problems.extras.vendingmachine;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String code) {
        super("item " + code + " is out of stock");
    }
}
