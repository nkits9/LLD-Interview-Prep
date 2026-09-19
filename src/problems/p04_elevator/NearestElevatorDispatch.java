package problems.p04_elevator;

import java.util.Comparator;
import java.util.List;

/** Nearest serviceable car; ties break by id for determinism. */
public class NearestElevatorDispatch implements DispatchStrategy {
    @Override
    public Elevator select(List<Elevator> elevators, int floor, Direction direction) {
        return elevators.stream()
                .filter(e -> e.state() != ElevatorState.MAINTENANCE)
                .min(Comparator.comparingInt((Elevator e) -> Math.abs(e.floor() - floor))
                        .thenComparing(Elevator::id))
                .orElseThrow(() -> new IllegalStateException("no elevator in service"));
    }
}
