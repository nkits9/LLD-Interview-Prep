package problems.p10_splitwise;

import java.util.List;
import java.util.Map;

/**
 * The split rule varies — Strategy. Money is long paise throughout.
 * params meaning depends on the type: EXACT = paise per user,
 * PERCENT = basis points (10000 = 100%), SHARE = weights, EQUAL = ignored.
 */
public interface SplitStrategy {
    /** Must return shares summing EXACTLY to totalPaise, or throw SplitValidationException. */
    Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> params);
}
