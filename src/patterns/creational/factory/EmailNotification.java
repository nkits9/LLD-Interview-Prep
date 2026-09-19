package patterns.creational.factory;

public class EmailNotification implements Notification {
    @Override
    public void send(String to, String message) {
        System.out.println("[email] to " + to + ": " + message);
    }
}
