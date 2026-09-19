package problems.p06_movieticketbooking;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Show {
    private final String id;
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public Show(String id, List<String> seatIds) {
        this.id = id;
        seatIds.forEach(seatId -> seats.put(seatId, new Seat(seatId)));
    }

    public String id() {
        return id;
    }

    Seat seat(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("no seat " + seatId + " in show " + id);
        }
        return seat;
    }

    public SeatStatus statusOf(String seatId) {
        return seat(seatId).status();
    }
}
