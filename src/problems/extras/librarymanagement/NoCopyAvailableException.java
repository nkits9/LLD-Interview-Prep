package problems.extras.librarymanagement;

public class NoCopyAvailableException extends RuntimeException {
    public NoCopyAvailableException(String isbn) {
        super("no copy of " + isbn + " available — reserve to queue for the next return");
    }
}
