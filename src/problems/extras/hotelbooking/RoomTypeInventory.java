package problems.extras.hotelbooking;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rooms of one type are FUNGIBLE — so inventory is a booked-count per NIGHT,
 * not per-room identity (contrast IRCTC, where one seat must span all
 * segments). A stay [checkIn, checkOut) consumes each night in the range,
 * all-or-nothing under the type's lock.
 */
public class RoomTypeInventory {
    private final int totalRooms;
    private final Map<LocalDate, Integer> bookedByNight = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RoomTypeInventory(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    /** Validate every night, then take every night — one critical section. */
    boolean tryBook(LocalDate checkIn, LocalDate checkOut, int rooms) {
        lock.lock();
        try {
            for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
                if (bookedByNight.getOrDefault(night, 0) + rooms > totalRooms) {
                    return false;                     // nothing mutated
                }
            }
            for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
                bookedByNight.merge(night, rooms, Integer::sum);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    void release(LocalDate checkIn, LocalDate checkOut, int rooms) {
        lock.lock();
        try {
            for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
                bookedByNight.merge(night, -rooms, Integer::sum);
            }
        } finally {
            lock.unlock();
        }
    }

    public int available(LocalDate night) {
        lock.lock();
        try {
            return totalRooms - bookedByNight.getOrDefault(night, 0);
        } finally {
            lock.unlock();
        }
    }
}
