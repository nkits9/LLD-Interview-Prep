package problems.extras.pizzaordering;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private final List<Pizza> items = new ArrayList<>();

    public Order add(Pizza pizza) {
        items.add(pizza);
        return this;
    }

    public long totalPaise() {
        return items.stream().mapToLong(Pizza::costPaise).sum();
    }

    public String bill() {
        StringBuilder sb = new StringBuilder("--- BILL ---\n");
        for (Pizza pizza : items) {
            sb.append(String.format("₹%7.2f  %s%n", pizza.costPaise() / 100.0, pizza.description()));
        }
        sb.append(String.format("₹%7.2f  TOTAL", totalPaise() / 100.0));
        return sb.toString();
    }
}
