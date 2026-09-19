package problems.extras.meetingscheduler;

import java.util.Comparator;
import java.util.List;

/** Don't burn the 10-seater on a 1:1 — smallest sufficient room first. */
public class SmallestFitRoomSelection implements RoomSelectionStrategy {
    @Override
    public List<Room> candidates(List<Room> rooms, int attendees) {
        return rooms.stream()
                .filter(room -> room.capacity() >= attendees)
                .sorted(Comparator.comparingInt(Room::capacity).thenComparing(Room::id))
                .toList();
    }
}
