package problems.extras.stockexchange;

/** A limit order; `remaining` shrinks as it fills (partial fills are normal). */
public class Order {
    private final String id;
    private final Side side;
    private final long pricePaise;
    private final int quantity;
    private final long seq;      // arrival order — the "time" in price-time priority
    private int remaining;

    Order(String id, Side side, long pricePaise, int quantity, long seq) {
        if (quantity < 1 || pricePaise < 1) {
            throw new IllegalArgumentException("price and quantity must be positive");
        }
        this.id = id;
        this.side = side;
        this.pricePaise = pricePaise;
        this.quantity = quantity;
        this.remaining = quantity;
        this.seq = seq;
    }

    public String id() {
        return id;
    }

    Side side() {
        return side;
    }

    long pricePaise() {
        return pricePaise;
    }

    public int remaining() {
        return remaining;
    }

    void fill(int qty) {
        remaining -= qty;
    }

    public boolean isFilled() {
        return remaining == 0;
    }

    @Override
    public String toString() {
        return id + " " + side + " " + remaining + "/" + quantity + "@₹" + pricePaise / 100;
    }
}
