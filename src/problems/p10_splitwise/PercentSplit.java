package problems.p10_splitwise;

import java.util.List;
import java.util.Map;

/** Percentages as BASIS POINTS (10000 = 100%) — never floating point near money. */
public class PercentSplit implements SplitStrategy {
    @Override
    public Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> params) {
        long basisPoints = participants.stream()
                .mapToLong(u -> params.getOrDefault(u, 0L)).sum();
        if (basisPoints != 10_000) {
            throw new SplitValidationException("percentages sum to " + basisPoints + " bp, need 10000");
        }
        return Proportional.split(totalPaise, participants, params);
    }
}
