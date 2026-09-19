package problems.extras.cabbooking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Request → surge quote → nearest-driver CAS claim → trip lifecycle.
 * Surge is locked in at request; fare is computed at completion from actual distance.
 */
public class RideService {
    private final Map<String, Driver> drivers = new ConcurrentHashMap<>();
    private final Map<String, Trip> trips = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();
    private final AtomicInteger activeTrips = new AtomicInteger();

    private final DriverMatchingStrategy matching;
    private final SurgeStrategy surge;
    private final FareStrategy fare;

    public RideService(DriverMatchingStrategy matching, SurgeStrategy surge, FareStrategy fare) {
        this.matching = matching;
        this.surge = surge;
        this.fare = fare;
    }

    public void register(Driver driver) {
        drivers.put(driver.id(), driver);
    }

    public Trip requestRide(String rider, Location pickup, Location drop) {
        int available = (int) drivers.values().stream().filter(Driver::isAvailable).count();
        int surgeBp = surge.surgeBasisPoints(activeTrips.get(), available);
        for (Driver candidate : matching.candidates(drivers.values(), pickup)) {
            if (candidate.tryClaim()) {                       // CAS: one request per driver
                Trip trip = new Trip("TRIP-" + seq.incrementAndGet(), rider,
                        candidate.id(), pickup, drop, surgeBp);
                trips.put(trip.id(), trip);
                activeTrips.incrementAndGet();
                return trip;
            }
        }
        throw new NoDriverAvailableException();
    }

    public void startTrip(String tripId) {
        trip(tripId).transitionTo(TripStatus.ONGOING);
    }

    /** Fare from actual distance × the surge locked at request time. */
    public long completeTrip(String tripId, int actualDistanceKm) {
        Trip trip = trip(tripId);
        trip.transitionTo(TripStatus.COMPLETED);
        long amount = fare.farePaise(actualDistanceKm, trip.surgeBasisPoints());
        trip.setFare(amount);
        Driver driver = drivers.get(trip.driverId());
        driver.moveTo(trip.drop());                           // driver ends up at the drop
        driver.release();
        activeTrips.decrementAndGet();
        return amount;
    }

    /** Only a not-yet-started trip can be cancelled (ONGOING → COMPLETED only). */
    public void cancelTrip(String tripId) {
        Trip trip = trip(tripId);
        trip.transitionTo(TripStatus.CANCELLED);
        drivers.get(trip.driverId()).release();
        activeTrips.decrementAndGet();
    }

    private Trip trip(String id) {
        Trip trip = trips.get(id);
        if (trip == null) {
            throw new IllegalArgumentException("no trip " + id);
        }
        return trip;
    }
}
