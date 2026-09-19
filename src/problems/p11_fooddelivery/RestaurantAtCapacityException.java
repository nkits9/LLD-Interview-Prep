package problems.p11_fooddelivery;

public class RestaurantAtCapacityException extends RuntimeException {
    public RestaurantAtCapacityException(String restaurantId) {
        super("restaurant " + restaurantId + " is at kitchen capacity — try later");
    }
}
