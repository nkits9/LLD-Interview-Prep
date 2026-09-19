package problems.extras.vendingmachine;

import java.util.List;
import java.util.Map;

class AcceptingMoneyState implements MachineState {
    @Override
    public String name() {
        return "ACCEPTING_MONEY";
    }

    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.escrowCoin(coin);
    }

    @Override
    public Map<Coin, Integer> selectItem(VendingMachine machine, String code) {
        return machine.vend(code); // atomic: item + exact change, or the sale is refused
    }

    @Override
    public List<Coin> cancel(VendingMachine machine) {
        return machine.refundEscrow();
    }
}
