package problems.extras.vendingmachine;

/** Denominations in paise — money is never a double. */
public enum Coin {
    ONE(100), TWO(200), FIVE(500), TEN(1000);

    private final long paise;

    Coin(long paise) {
        this.paise = paise;
    }

    public long paise() {
        return paise;
    }
}
