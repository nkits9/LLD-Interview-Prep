package problems.p11_fooddelivery;

public class CancellationNotAllowedException extends IllegalStateException {
    public CancellationNotAllowedException(OrderStatus status) {
        super("cannot cancel an order that is " + status);
    }
}
