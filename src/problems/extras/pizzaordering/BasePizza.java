package problems.extras.pizzaordering;

/**
 * Immutable base built via Builder: required size in the builder constructor
 * (compile-time enforced), optional crust with a default, price derived once.
 */
public final class BasePizza implements Pizza {
    public enum Size {
        SMALL(15_000), MEDIUM(22_000), LARGE(30_000);

        final long basePaise;

        Size(long basePaise) {
            this.basePaise = basePaise;
        }
    }

    public enum Crust {
        THIN(0), PAN(3_000), CHEESE_BURST(6_000);

        final long extraPaise;

        Crust(long extraPaise) {
            this.extraPaise = extraPaise;
        }
    }

    private final Size size;
    private final Crust crust;

    private BasePizza(Builder builder) {
        this.size = builder.size;
        this.crust = builder.crust;
    }

    public static Builder ofSize(Size size) {
        return new Builder(size);
    }

    public static final class Builder {
        private final Size size;   // required → builder constructor
        private Crust crust = Crust.THIN;

        private Builder(Size size) {
            this.size = size;
        }

        public Builder crust(Crust crust) {
            this.crust = crust;
            return this;
        }

        public BasePizza build() {
            return new BasePizza(this);
        }
    }

    @Override
    public long costPaise() {
        return size.basePaise + crust.extraPaise;
    }

    @Override
    public String description() {
        return size + " " + crust + " pizza";
    }
}
