package problems.p11_fooddelivery;

public class IllegalTransitionException extends IllegalStateException {
    public IllegalTransitionException(OrderStatus from, OrderStatus to) {
        super("illegal transition " + from + " -> " + to);
    }
}
