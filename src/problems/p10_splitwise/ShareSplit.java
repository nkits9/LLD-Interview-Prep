package problems.p10_splitwise;

import java.util.List;
import java.util.Map;

/** Weight-based (2:1:1 …) — same largest-remainder distribution as percent. */
public class ShareSplit implements SplitStrategy {
    @Override
    public Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> params) {
        return Proportional.split(totalPaise, participants, params);
    }
}
