package patterns.behavioral.state;

/** Terminal: overrides nothing, so every action throws. */
class CancelledState implements OrderState {
    @Override
    public String name() {
        return "CANCELLED";
    }
}
