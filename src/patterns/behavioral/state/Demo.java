package patterns.behavioral.state;

public class Demo {
    public static void main(String[] args) {
        // Happy path: PLACED -> PAID -> SHIPPED
        Order order1 = new Order("ORD-1");
        System.out.println(order1.id() + " starts   : " + order1.status());
        order1.pay();
        System.out.println(order1.id() + " after pay: " + order1.status());
        order1.ship();
        System.out.println(order1.id() + " shipped  : " + order1.status());

        // Failure path 1: shipping an unpaid order is rejected explicitly.
        Order order2 = new Order("ORD-2");
        try {
            order2.ship();
        } catch (IllegalStateTransitionException e) {
            System.out.println(order2.id() + " rejected : " + e.getMessage());
        }
        order2.cancel(); // free cancel before payment
        System.out.println(order2.id() + " cancelled: " + order2.status());

        // Failure path 2: cancelling after shipment is rejected explicitly.
        try {
            order1.cancel();
        } catch (IllegalStateTransitionException e) {
            System.out.println(order1.id() + " rejected : " + e.getMessage());
        }
    }
}
