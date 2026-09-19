package problems.extras.librarymanagement;

import java.time.LocalDate;

public final class Loan {
    private final String copyId;
    private final String memberId;
    private final LocalDate dueDate;

    Loan(String copyId, String memberId, LocalDate dueDate) {
        this.copyId = copyId;
        this.memberId = memberId;
        this.dueDate = dueDate;
    }

    public String copyId() {
        return copyId;
    }

    public String memberId() {
        return memberId;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        return copyId + " -> " + memberId + " (due " + dueDate + ")";
    }
}
