package problems.p03_parkinglot;

import java.util.EnumSet;
import java.util.Set;

/** The compatibility MATRIX lives on the enum — call sites never if-else on types. */
public enum SpotType {
    SMALL(EnumSet.of(VehicleType.BIKE)),
    MEDIUM(EnumSet.of(VehicleType.BIKE, VehicleType.CAR)),
    LARGE(EnumSet.allOf(VehicleType.class));

    private final Set<VehicleType> fits;

    SpotType(Set<VehicleType> fits) {
        this.fits = fits;
    }

    public boolean canFit(VehicleType vehicleType) {
        return fits.contains(vehicleType);
    }
}
