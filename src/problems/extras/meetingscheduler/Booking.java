package problems.extras.meetingscheduler;

import java.time.Instant;

/** Half-open interval [start, end) — back-to-back meetings never conflict. */
public final class Booking {
    private final String id;
    private final String roomId;
    private final String organizer;
    private final Instant start;
    private final Instant end;
    private final int attendees;

    Booking(String id, String roomId, String organizer, Instant start, Instant end, int attendees) {
        this.id = id;
        this.roomId = roomId;
        this.organizer = organizer;
        this.start = start;
        this.end = end;
        this.attendees = attendees;
    }

    public String id() {
        return id;
    }

    public String roomId() {
        return roomId;
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    @Override
    public String toString() {
        return id + " " + roomId + " " + organizer + " " + start + "->" + end + " (" + attendees + "p)";
    }
}
