package problems.extras.meetingscheduler;

import java.util.List;

/** Allocation varies (smallest fit, nearest floor, AV-equipped) — Strategy. */
public interface RoomSelectionStrategy {
    List<Room> candidates(List<Room> rooms, int attendees);
}
