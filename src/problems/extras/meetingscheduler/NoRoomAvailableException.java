package problems.extras.meetingscheduler;

public class NoRoomAvailableException extends RuntimeException {
    public NoRoomAvailableException(int attendees) {
        super("no room free for " + attendees + " people in that slot");
    }
}
