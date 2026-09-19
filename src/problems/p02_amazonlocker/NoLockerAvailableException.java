package problems.p02_amazonlocker;

/** Too large for any locker, or all compatible lockers occupied. */
public class NoLockerAvailableException extends RuntimeException {
    public NoLockerAvailableException(String reason) {
        super(reason);
    }
}
