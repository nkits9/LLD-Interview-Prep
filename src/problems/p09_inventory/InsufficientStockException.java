package problems.p09_inventory;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int requested, int available) {
        super("sku " + sku + ": requested " + requested + ", available " + available);
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
