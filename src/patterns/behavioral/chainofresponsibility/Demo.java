package patterns.behavioral.chainofresponsibility;

public class Demo {
    public static void main(String[] args) {
        Filter chain = new AuthFilter();
        chain.linkWith(new RateLimitFilter(2));

        // Passes both filters.
        chain.handle(new Request("ankit", "tok-123", "GET /bookings"));
        chain.handle(new Request("ankit", "tok-123", "GET /bookings"));

        // Failure path 1: third request trips the rate limit.
        try {
            chain.handle(new Request("ankit", "tok-123", "GET /bookings"));
        } catch (RequestRejectedException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Failure path 2: no token — rejected at the FIRST link;
        // the rate limiter never sees it (early termination).
        try {
            chain.handle(new Request("mallory", null, "GET /bookings"));
        } catch (RequestRejectedException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
