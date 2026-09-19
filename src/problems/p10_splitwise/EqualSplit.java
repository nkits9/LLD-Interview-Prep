package problems.p10_splitwise;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ₹100 ÷ 3 = 3334 + 3333 + 3333: the first (total % n) participants get the
 * extra paisa — a DETERMINISTIC, documented rule, not floating-point luck.
 */
public class EqualSplit implements SplitStrategy {
    @Override
    public Map<User, Long> split(long totalPaise, List<User> participants, Map<User, Long> params) {
        long base = totalPaise / participants.size();
        long extras = totalPaise % participants.size();
        Map<User, Long> shares = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            shares.put(participants.get(i), base + (i < extras ? 1 : 0));
        }
        return shares;
    }
}
