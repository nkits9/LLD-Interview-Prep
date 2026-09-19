package problems.extras.hotelbooking;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Hotel {
    private final Map<RoomType, RoomTypeInventory> inventory = new EnumMap<>(RoomType.class);
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public void addRooms(RoomType type, int count) {
        inventory.put(type, new RoomTypeInventory(count));
    }

    public Booking book(String guest, RoomType type, LocalDate checkIn, LocalDate checkOut, int rooms) {
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("check-in must be before check-out");
        }
        if (rooms < 1) {
            throw new IllegalArgumentException("rooms must be >= 1");
        }
        RoomTypeInventory typeInventory = inventoryFor(type);
        if (!typeInventory.tryBook(checkIn, checkOut, rooms)) {
            throw new NoAvailabilityException(rooms + "x" + type + " not available every night of "
                    + checkIn + " -> " + checkOut);
        }
        Booking booking = new Booking("HB-" + seq.incrementAndGet(), guest, type,
                checkIn, checkOut, rooms);
        bookings.put(booking.id(), booking);
        return booking;
    }

    public void cancel(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("no booking " + bookingId);
        }
        booking.markCancelled(); // atomic: double-cancel throws, rooms released exactly once
        inventoryFor(booking.type()).release(booking.checkIn(), booking.checkOut(), booking.rooms());
    }

    public int available(RoomType type, LocalDate night) {
        return inventoryFor(type).available(night);
    }

    private RoomTypeInventory inventoryFor(RoomType type) {
        RoomTypeInventory typeInventory = inventory.get(type);
        if (typeInventory == null) {
            throw new IllegalArgumentException("hotel has no " + type + " rooms");
        }
        return typeInventory;
    }
}
