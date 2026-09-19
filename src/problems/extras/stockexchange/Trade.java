package problems.extras.stockexchange;

/** An execution: always priced at the RESTING order's limit (price improvement). */
public final class Trade {
    private final String buyOrderId;
    private final String sellOrderId;
    private final long pricePaise;
    private final int quantity;

    Trade(String buyOrderId, String sellOrderId, long pricePaise, int quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.pricePaise = pricePaise;
        this.quantity = quantity;
    }

    public int quantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return quantity + "@₹" + pricePaise / 100 + " (" + buyOrderId + " x " + sellOrderId + ")";
    }
}
