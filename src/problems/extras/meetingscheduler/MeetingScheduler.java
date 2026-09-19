package problems.extras.meetingscheduler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Walks the strategy's candidates; each room's tryBook is the atomic gate. */
public class MeetingScheduler {
    private final List<Room> rooms;
    private final RoomSelectionStrategy selection;
    private final Map<String, Booking> byId = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public MeetingScheduler(List<Room> rooms, RoomSelectionStrategy selection) {
        this.rooms = List.copyOf(rooms);
        this.selection = selection;
    }

    public Booking book(String organizer, int attendees, Instant start, Instant end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        for (Room room : selection.candidates(rooms, attendees)) {
            Booking booking = new Booking("MTG-" + seq.incrementAndGet(), room.id(),
                    organizer, start, end, attendees);
            if (room.tryBook(booking)) {
                byId.put(booking.id(), booking);
                return booking;
            }
        }
        throw new NoRoomAvailableException(attendees);
    }

    public void cancel(String bookingId) {
        Booking booking = byId.remove(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("no booking " + bookingId);
        }
        rooms.stream().filter(r -> r.id().equals(booking.roomId()))
                .findFirst().ifPresent(r -> r.cancel(bookingId));
    }
}
