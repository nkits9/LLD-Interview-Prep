package problems.p04_elevator;

/** Bounded pending stops — backpressure instead of unbounded growth. */
public class RequestQueueFullException extends RuntimeException {
    public RequestQueueFullException(String elevatorId, int bound) {
        super("elevator " + elevatorId + " already has " + bound + " pending stops");
    }
}
