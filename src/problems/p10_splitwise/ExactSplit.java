package problems.p10_splitwise;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caller names every share; the sum must match to the paisa. */
public class ExactSplit implements SplitStrategy {
    @Override
    public Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> params) {
        long sum = 0;
        Map<User, Long> shares = new LinkedHashMap<>();
        for (User user : participants) {
            Long amount = params.get(user);
            if (amount == null || amount < 0) {
                throw new SplitValidationException("exact split needs a non-negative amount for " + user);
            }
            shares.put(user, amount);
            sum += amount;
        }
        if (sum != totalPaise) {
            throw new SplitValidationException(
                    "exact shares sum to " + sum + " paise but total is " + totalPaise);
        }
        return shares;
    }
}
