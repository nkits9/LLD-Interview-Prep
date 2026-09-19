package problems.p06_movieticketbooking;

import java.util.List;

/** All-or-nothing: naming exactly which seats blocked the request. */
public class SeatsUnavailableException extends RuntimeException {
    public SeatsUnavailableException(List<String> seatIds) {
        super("seats unavailable: " + seatIds);
    }
}
