package problems.extras.irctc;

/** Unknown station, or destination not after source on this train's route. */
public class InvalidRouteException extends IllegalArgumentException {
    public InvalidRouteException(String reason) {
        super(reason);
    }
}
