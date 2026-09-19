package patterns.behavioral.state;

/**
 * Every action is illegal by default; each state overrides ONLY what it allows.
 * A new lifecycle stage is a new class — no switch to grow.
 */
public interface OrderState {
    String name();

    default void pay(Order order) {
        throw new IllegalStateTransitionException(name(), "pay");
    }

    default void ship(Order order) {
        throw new IllegalStateTransitionException(name(), "ship");
    }

    default void cancel(Order order) {
        throw new IllegalStateTransitionException(name(), "cancel");
    }
}
