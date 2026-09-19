package patterns.creational.factory;

public class SmsNotification implements Notification {
    @Override
    public void send(String to, String message) {
        System.out.println("[sms]   to " + to + ": " + message);
    }
}
