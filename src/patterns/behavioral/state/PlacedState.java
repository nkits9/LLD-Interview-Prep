package patterns.behavioral.state;

class PlacedState implements OrderState {
    @Override
    public String name() {
        return "PLACED";
    }

    @Override
    public void pay(Order order) {
        order.transitionTo(new PaidState());
    }

    @Override
    public void cancel(Order order) {
        order.transitionTo(new CancelledState()); // free before payment
    }
}
