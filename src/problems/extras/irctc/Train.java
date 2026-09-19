package problems.extras.irctc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A train = an ordered route + per-date runs. Segment k is the hop from
 * stations[k] to stations[k+1]. Runs once a day; the reverse journey is a
 * different train id (assumption).
 */
public class Train {
    private final String id;
    private final List<String> stations;
    private final int seatsPerRun;
    private final Map<LocalDate, TrainRun> runs = new ConcurrentHashMap<>();

    public Train(String id, List<String> stations, int seatsPerRun) {
        if (stations.size() < 2) {
            throw new IllegalArgumentException("route needs at least 2 stations");
        }
        if (stations.size() > 64) {
            throw new IllegalArgumentException("more than 64 stations: use a BitSet occupancy, not a long mask");
        }
        if (Set.copyOf(stations).size() != stations.size()) {
            throw new IllegalArgumentException("route has duplicate stations: " + stations);
        }
        if (seatsPerRun < 1) {
            throw new IllegalArgumentException("train needs at least 1 seat");
        }
        this.id = id;
        this.stations = List.copyOf(stations);
        this.seatsPerRun = seatsPerRun;
    }

    public String id() {
        return id;
    }

    public List<String> stations() {
        return stations;
    }

    /** Does this train visit {@code from} strictly before {@code to}? */
    public boolean covers(String from, String to) {
        int i = stations.indexOf(from);
        int j = stations.indexOf(to);
        return i >= 0 && j >= 0 && i < j;
    }

    /** Bits i..j-1 — the segments a from→to journey occupies. Validates the route. */
    public long segmentMask(String from, String to) {
        int i = stations.indexOf(from);
        int j = stations.indexOf(to);
        if (i < 0 || j < 0 || i >= j) {
            throw new InvalidRouteException(id + " does not run " + from + " -> " + to);
        }
        return ((1L << j) - 1) & ~((1L << i) - 1);
    }

    /** Lazy per-date inventory — compute-if-absent, no check-then-act race. */
    public TrainRun runOn(LocalDate date) {
        return runs.computeIfAbsent(date, d -> new TrainRun(seatsPerRun));
    }
}
