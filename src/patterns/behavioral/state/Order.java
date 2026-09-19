package patterns.behavioral.state;

/**
 * Context. Delegates every action to the current state — no switch, no
 * if (status == ...) chains anywhere.
 */
public class Order {
    private final String id;
    private OrderState state = new PlacedState();

    public Order(String id) {
        this.id = id;
    }

    // synchronized: a transition must be atomic — two threads acting on the
    // same order must not both pass the same state's checks.
    public synchronized void pay() {
        state.pay(this);
    }

    public synchronized void ship() {
        state.ship(this);
    }

    public synchronized void cancel() {
        state.cancel(this);
    }

    // package-private: only states move the machine; callers can't force a state.
    void transitionTo(OrderState next) {
        this.state = next;
    }

    public String status() {
        return state.name();
    }

    public String id() {
        return id;
    }
}
