package problems.extras.meetingscheduler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bookings in a TreeMap keyed by start time — overlap needs exactly TWO
 * neighbor checks (floor + ceiling), O(log n), never a scan. Per-room lock:
 * rooms never contend with each other.
 */
public class Room {
    private final String id;
    private final int capacity;
    private final TreeMap<Instant, Booking> bookings = new TreeMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String id() {
        return id;
    }

    public int capacity() {
        return capacity;
    }

    /** Check-and-insert under one lock — no check-then-act window. */
    boolean tryBook(Booking booking) {
        lock.lock();
        try {
            Map.Entry<Instant, Booking> before = bookings.floorEntry(booking.start());
            if (before != null && before.getValue().end().isAfter(booking.start())) {
                return false;              // previous meeting still running
            }
            Map.Entry<Instant, Booking> after = bookings.ceilingEntry(booking.start());
            if (after != null && after.getKey().isBefore(booking.end())) {
                return false;              // next meeting starts before we finish
            }
            bookings.put(booking.start(), booking);
            return true;
        } finally {
            lock.unlock();
        }
    }

    boolean cancel(String bookingId) {
        lock.lock();
        try {
            return bookings.values().removeIf(b -> b.id().equals(bookingId));
        } finally {
            lock.unlock();
        }
    }

    public List<Booking> schedule() {
        lock.lock();
        try {
            return List.copyOf(bookings.values());
        } finally {
            lock.unlock();
        }
    }
}
