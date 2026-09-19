package problems.extras.vendingmachine;

class IdleState implements MachineState {
    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.escrowCoin(coin);
        machine.transitionTo(new AcceptingMoneyState());
    }
}
