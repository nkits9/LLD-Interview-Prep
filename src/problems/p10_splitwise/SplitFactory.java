package problems.p10_splitwise;

import java.util.EnumMap;
import java.util.Map;

/** Strategy picked by type — the Factory pairing; strategies are stateless and shared. */
public final class SplitFactory {
    private static final Map<SplitType, SplitStrategy> REGISTRY = new EnumMap<>(SplitType.class);

    static {
        REGISTRY.put(SplitType.EQUAL, new EqualSplit());
        REGISTRY.put(SplitType.EXACT, new ExactSplit());
        REGISTRY.put(SplitType.PERCENT, new PercentSplit());
        REGISTRY.put(SplitType.SHARE, new ShareSplit());
    }

    private SplitFactory() {
    }

    public static SplitStrategy of(SplitType type) {
        return REGISTRY.get(type);
    }
}
