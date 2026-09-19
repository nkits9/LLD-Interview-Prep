package problems.extras.librarymanagement;

public class BorrowLimitExceededException extends RuntimeException {
    public BorrowLimitExceededException(String memberId, int limit) {
        super(memberId + " already has " + limit + " books out");
    }
}
