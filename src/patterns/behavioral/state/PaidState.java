package patterns.behavioral.state;

class PaidState implements OrderState {
    @Override
    public String name() {
        return "PAID";
    }

    @Override
    public void ship(Order order) {
        order.transitionTo(new ShippedState());
    }

    @Override
    public void cancel(Order order) {
        // refund hook would go here (saga: cancel compensates the payment)
        order.transitionTo(new CancelledState());
    }
}
