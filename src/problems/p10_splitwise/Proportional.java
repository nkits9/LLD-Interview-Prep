package problems.p10_splitwise;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Largest-remainder method: floor every share, then hand the leftover paise to
 * the largest fractional remainders (ties by participant order). Deterministic,
 * and the shares always sum exactly to the total.
 */
final class Proportional {
    private Proportional() {
    }

    static Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> weights) {
        long weightSum = 0;
        for (User user : participants) {
            Long w = weights.get(user);
            if (w == null || w <= 0) {
                throw new SplitValidationException("positive weight required for " + user);
            }
            weightSum += w;
        }
        Map<User, Long> shares = new LinkedHashMap<>();
        List<User> byRemainder = new ArrayList<>(participants);
        long distributed = 0;
        Map<User, Long> remainders = new LinkedHashMap<>();
        for (User user : participants) {
            long numerator = totalPaise * weights.get(user);
            shares.put(user, numerator / weightSum);
            remainders.put(user, numerator % weightSum);
            distributed += numerator / weightSum;
        }
        long leftover = totalPaise - distributed;
        byRemainder.sort((a, b) -> Long.compare(remainders.get(b), remainders.get(a))); // stable: ties keep order
        for (int i = 0; i < leftover; i++) {
            User user = byRemainder.get(i);
            shares.put(user, shares.get(user) + 1);
        }
        return shares;
    }
}
