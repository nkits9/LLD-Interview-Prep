package problems.extras.vendingmachine;

import java.util.List;
import java.util.Map;

/** Every operation is illegal by default; each state overrides what it allows. */
public interface MachineState {
    String name();

    default void insertCoin(VendingMachine machine, Coin coin) {
        throw new IllegalStateException("cannot insert coins while " + name());
    }

    default Map<Coin, Integer> selectItem(VendingMachine machine, String code) {
        throw new IllegalStateException("cannot select an item while " + name());
    }

    default List<Coin> cancel(VendingMachine machine) {
        throw new IllegalStateException("nothing to cancel while " + name());
    }
}
