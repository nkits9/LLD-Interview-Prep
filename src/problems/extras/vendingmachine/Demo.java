package problems.extras.vendingmachine;

public class Demo {
    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine();
        machine.load(new Item("A1", "chips", 2_000), 2);   // ₹20
        machine.load(new Item("B1", "soda", 3_300), 1);    // ₹33
        machine.loadCoins(Coin.FIVE, 1);                   // till has just one ₹5 coin

        // Failure: state blocks selection before any money is in.
        try {
            machine.selectItem("A1");
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Happy path: ₹25 in for a ₹20 item → chips + ₹5 change.
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.FIVE);
        System.out.println("bought A1, change: " + machine.selectItem("A1") + ", state " + machine.stateName());

        // Insufficient funds — coins stay in escrow, user can top up or cancel.
        machine.insertCoin(Coin.TEN);
        try {
            machine.selectItem("B1");
        } catch (InsufficientFundsException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        System.out.println("cancelled, refund: " + machine.cancel());

        // Cannot make change → sale refused, full refund on cancel.
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.FIVE);                     // ₹35 for a ₹33 soda; no ₹1/₹2 coins anywhere
        try {
            machine.selectItem("B1");
        } catch (CannotMakeChangeException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        System.out.println("cancelled, refund: " + machine.cancel());

        // Out of stock (A1 had 2; one sold; sell the second, then a third attempt fails).
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.TEN);
        machine.selectItem("A1");
        machine.insertCoin(Coin.TEN);
        machine.insertCoin(Coin.TEN);
        try {
            machine.selectItem("A1");
        } catch (OutOfStockException e) {
            System.out.println("rejected: " + e.getMessage());
            System.out.println("cancelled, refund: " + machine.cancel());
        }
    }
}
