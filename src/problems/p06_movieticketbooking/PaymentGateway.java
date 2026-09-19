package problems.p06_movieticketbooking;

/** Injected (DIP): the service never knows which PSP; tests use a scriptable fake. */
public interface PaymentGateway {
    boolean charge(String user, long amountPaise, String idempotencyKey);

    void refund(String user, long amountPaise);
}
