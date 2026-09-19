package problems.extras.pizzaordering;

/**
 * ONE decorator class, configured by data — Cheese/Olive/Paneer classes that
 * differ only in name and price are ceremony, not design (say this aloud).
 */
public final class Topping implements Pizza {
    private final Pizza inner;
    private final String name;
    private final long pricePaise;

    public Topping(Pizza inner, String name, long pricePaise) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("topping needs a name");
        }
        if (pricePaise < 0) {
            throw new IllegalArgumentException("topping price cannot be negative");
        }
        this.inner = inner;
        this.name = name;
        this.pricePaise = pricePaise;
    }

    @Override
    public long costPaise() {
        return inner.costPaise() + pricePaise; // is-a Pizza, has-a Pizza
    }

    @Override
    public String description() {
        return inner.description() + " + " + name;
    }
}
