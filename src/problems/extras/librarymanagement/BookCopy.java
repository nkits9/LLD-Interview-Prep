package problems.extras.librarymanagement;

/** A physical copy — the thing that's actually lent; the Book is just metadata. */
public class BookCopy {
    private final String copyId;
    private final String isbn;
    private boolean available = true;

    BookCopy(String copyId, String isbn) {
        this.copyId = copyId;
        this.isbn = isbn;
    }

    public String copyId() {
        return copyId;
    }

    String isbn() {
        return isbn;
    }

    boolean isAvailable() {
        return available;
    }

    void setAvailable(boolean available) {
        this.available = available;
    }
}
