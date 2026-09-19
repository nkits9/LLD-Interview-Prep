package problems.p03_parkinglot;

public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String reason) {
        super(reason);
    }
}
