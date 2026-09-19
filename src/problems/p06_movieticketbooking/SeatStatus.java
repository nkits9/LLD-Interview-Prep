package problems.p06_movieticketbooking;

/** Seat lifecycle: AVAILABLE -> HELD (TTL) -> BOOKED, back to AVAILABLE on release/cancel. */
public enum SeatStatus {
    AVAILABLE, HELD, BOOKED
}
