package patterns.behavioral.chainofresponsibility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Per-user counter — atomic increment, no check-then-act race. */
public class RateLimitFilter extends Filter {
    private final int maxRequests;
    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    @Override
    protected void check(Request request) {
        int used = counts.computeIfAbsent(request.user(), u -> new AtomicInteger())
                .incrementAndGet();
        if (used > maxRequests) {
            throw new RequestRejectedException("rate-limit", request.user() + " exceeded " + maxRequests + " requests");
        }
        System.out.println("[rate]  ok (" + used + "/" + maxRequests + ") for " + request.user());
    }
}
