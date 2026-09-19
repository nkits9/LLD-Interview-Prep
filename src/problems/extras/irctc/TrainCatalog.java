package problems.extras.irctc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Repository + search: by train number, and by (source before destination). */
public class TrainCatalog {
    private final Map<String, Train> trains = new ConcurrentHashMap<>();

    public void register(Train train) {
        trains.put(train.id(), train);
    }

    public Train byNumber(String trainId) {
        Train train = trains.get(trainId);
        if (train == null) {
            throw new IllegalArgumentException("no train with number " + trainId);
        }
        return train;
    }

    /** Every train runs daily (assumption), so route match is the whole search. */
    public List<Train> search(String from, String to) {
        return trains.values().stream()
                .filter(train -> train.covers(from, to))
                .collect(Collectors.toList());
    }
}
