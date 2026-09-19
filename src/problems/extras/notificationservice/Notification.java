package problems.extras.notificationservice;

public final class Notification {
    private final String userId;
    private final String message;

    public Notification(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String userId() {
        return userId;
    }

    public String message() {
        return message;
    }
}
