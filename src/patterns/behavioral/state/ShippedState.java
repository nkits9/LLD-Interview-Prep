package patterns.behavioral.state;

/** Terminal in our scope: overrides nothing, so every action throws. */
class ShippedState implements OrderState {
    @Override
    public String name() {
        return "SHIPPED";
    }
}
