package problems.extras.vendingmachine;

public final class Item {
    private final String code;
    private final String name;
    private final long pricePaise;

    public Item(String code, String name, long pricePaise) {
        this.code = code;
        this.name = name;
        this.pricePaise = pricePaise;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public long pricePaise() {
        return pricePaise;
    }
}
