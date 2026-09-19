package problems.extras.irctc;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String ticketId) {
        super("no ticket with id " + ticketId);
    }
}
