package problems.extras.librarymanagement;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositories (Map<Id, Entity>) + the two rules that matter: fines are
 * computed from an injected clock, and a returned copy goes to the FIRST
 * reservation in the queue before it becomes generally available.
 */
public class LibraryService {
    private static final int BORROW_LIMIT = 3;
    private static final int LOAN_DAYS = 14;
    private static final long FINE_PER_DAY_PAISE = 1_000; // ₹10/day

    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, BookCopy> copies = new LinkedHashMap<>();
    private final Map<String, Loan> loansByCopy = new LinkedHashMap<>();
    private final Map<String, Deque<String>> reservations = new LinkedHashMap<>(); // isbn -> members
    private final AtomicLong seq = new AtomicLong();
    private final Clock clock;

    public LibraryService(Clock clock) {
        this.clock = clock;
    }

    public void addBook(Book book, int copyCount) {
        books.put(book.isbn(), book);
        for (int i = 0; i < copyCount; i++) {
            BookCopy copy = new BookCopy(book.isbn() + "#" + seq.incrementAndGet(), book.isbn());
            copies.put(copy.copyId(), copy);
        }
    }

    public synchronized Loan checkout(String memberId, String isbn) {
        if (!books.containsKey(isbn)) {
            throw new IllegalArgumentException("no book " + isbn);
        }
        long onLoan = loansByCopy.values().stream()
                .filter(l -> l.memberId().equals(memberId)).count();
        if (onLoan >= BORROW_LIMIT) {
            throw new BorrowLimitExceededException(memberId, BORROW_LIMIT);
        }
        Optional<BookCopy> free = copies.values().stream()
                .filter(c -> c.isbn().equals(isbn)).filter(BookCopy::isAvailable).findFirst();
        if (free.isEmpty()) {
            throw new NoCopyAvailableException(isbn);
        }
        return lend(free.get(), memberId);
    }

    public synchronized void reserve(String memberId, String isbn) {
        reservations.computeIfAbsent(isbn, k -> new ArrayDeque<>()).addLast(memberId);
    }

    /**
     * Returns the fine in paise (0 when on time). The copy is handed straight
     * to the next reservation, if any — reservations beat the open shelf.
     */
    public synchronized long returnCopy(String copyId) {
        Loan loan = loansByCopy.remove(copyId);
        if (loan == null) {
            throw new IllegalArgumentException("copy " + copyId + " is not on loan");
        }
        BookCopy copy = copies.get(copyId);
        copy.setAvailable(true);

        long finePaise = 0;
        LocalDate today = LocalDate.now(clock);
        if (today.isAfter(loan.dueDate())) {
            finePaise = ChronoUnit.DAYS.between(loan.dueDate(), today) * FINE_PER_DAY_PAISE;
        }

        Deque<String> queue = reservations.get(copy.isbn());
        if (queue != null && !queue.isEmpty()) {
            String nextMember = queue.pollFirst();
            Loan next = lend(copy, nextMember); // reservation honored before the open shelf
            System.out.println("[library] reserved copy auto-issued: " + next);
        }
        return finePaise;
    }

    private Loan lend(BookCopy copy, String memberId) {
        copy.setAvailable(false);
        Loan loan = new Loan(copy.copyId(), memberId,
                LocalDate.now(clock).plus(Period.ofDays(LOAN_DAYS)));
        loansByCopy.put(copy.copyId(), loan);
        return loan;
    }

    public synchronized long availableCopies(String isbn) {
        return copies.values().stream()
                .filter(c -> c.isbn().equals(isbn)).filter(BookCopy::isAvailable).count();
    }
}
