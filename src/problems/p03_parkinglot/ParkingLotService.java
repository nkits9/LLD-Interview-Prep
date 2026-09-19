package problems.p03_parkinglot;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Entry/exit orchestration. Holds no lock — parallel gates contend per-spot
 * via CAS. Active tickets are the repository (Map<Id, Ticket>).
 */
public class ParkingLotService {
    private final List<Spot> spots;
    private final SpotAllocationStrategy allocation;
    private final PricingStrategy pricing;
    private final Clock clock;                       // injected: fees are testable
    private final long lostTicketPenaltyPaise;
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final AtomicLong ticketSeq = new AtomicLong();

    public ParkingLotService(List<Spot> spots, SpotAllocationStrategy allocation,
                             PricingStrategy pricing, Clock clock, long lostTicketPenaltyPaise) {
        this.spots = List.copyOf(spots);
        this.allocation = allocation;
        this.pricing = pricing;
        this.clock = clock;
        this.lostTicketPenaltyPaise = lostTicketPenaltyPaise;
    }

    /** CAS down the candidate list; all losses → clean LotFullException. */
    public Ticket park(Vehicle vehicle) {
        for (Spot spot : allocation.candidates(spots, vehicle.type())) {
            if (spot.tryPark(vehicle)) {
                Ticket ticket = new Ticket("TKT-" + ticketSeq.incrementAndGet(),
                        vehicle, spot.id(), clock.instant());
                activeTickets.put(ticket.id(), ticket);
                return ticket;
            }
        }
        throw new LotFullException(vehicle.type());
    }

    /** Normal exit: fee from actual duration, spot freed atomically. */
    public long unpark(String ticketId) {
        Ticket ticket = activeTickets.remove(ticketId); // atomic claim of the exit
        if (ticket == null) {
            throw new InvalidTicketException("unknown or already-used ticket " + ticketId);
        }
        Duration parked = Duration.between(ticket.entryTime(), clock.instant());
        long fee = pricing.fee(ticket.vehicle().type(), parked);
        freeSpot(ticket);
        return fee;
    }

    /** Lost ticket: locate by plate, charge the flat penalty (a policy, not an accident). */
    public long exitWithLostTicket(String plate) {
        Ticket ticket = activeTickets.values().stream()
                .filter(t -> t.vehicle().plate().equals(plate))
                .findFirst()
                .orElseThrow(() -> new InvalidTicketException("no active parking for plate " + plate));
        activeTickets.remove(ticket.id());
        freeSpot(ticket);
        return lostTicketPenaltyPaise;
    }

    public long freeSpotCount(VehicleType type) {
        return spots.stream().filter(s -> s.type().canFit(type)).filter(Spot::isFree).count();
    }

    private void freeSpot(Ticket ticket) {
        spots.stream().filter(s -> s.id().equals(ticket.spotId())).findFirst()
                .ifPresent(s -> s.free(ticket.vehicle()));
    }
}
