package problems.p03_parkinglot;

public class LotFullException extends RuntimeException {
    public LotFullException(VehicleType type) {
        super("no free spot can fit a " + type);
    }
}
