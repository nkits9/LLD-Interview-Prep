package problems.p07_loggingservice;

/** What to do when the bounded queue is full — a STATED decision, never an unbounded queue. */
public enum RejectionPolicy {
    BLOCK,       // backpressure: caller waits
    DROP_NEWEST  // shed load: count and drop the incoming record
}
