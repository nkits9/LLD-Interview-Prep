package problems.extras.pizzaordering;

public class Demo {
    public static void main(String[] args) {
        // Builder assembles the base; decorators stack the toppings.
        Pizza margherita = new Topping(
                BasePizza.ofSize(BasePizza.Size.MEDIUM).build(),
                "extra cheese", 4_000);

        Pizza loaded = new Topping(new Topping(new Topping(
                BasePizza.ofSize(BasePizza.Size.LARGE).crust(BasePizza.Crust.CHEESE_BURST).build(),
                "paneer", 6_000),
                "olives", 3_000),
                "paneer", 6_000); // same topping twice — stacking is free with decorators

        Order order = new Order().add(margherita).add(loaded);
        System.out.println(order.bill());

        // Validation lives in one place per class.
        try {
            new Topping(margherita, " ", 1_000);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            new Topping(margherita, "gold leaf", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
