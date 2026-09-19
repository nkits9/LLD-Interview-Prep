package problems.p03_parkinglot;

/** One class + static factory: subtypes would only earn their keep with real per-type behaviour. */
public final class Vehicle {
    private final String plate;
    private final VehicleType type;

    private Vehicle(String plate, VehicleType type) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("plate is required");
        }
        this.plate = plate;
        this.type = type;
    }

    public static Vehicle of(VehicleType type, String plate) {
        return new Vehicle(plate, type);
    }

    public String plate() {
        return plate;
    }

    public VehicleType type() {
        return type;
    }

    @Override
    public String toString() {
        return plate + "(" + type + ")";
    }
}
