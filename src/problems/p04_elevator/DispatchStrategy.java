package problems.p04_elevator;

import java.util.List;

/** Dispatch varies (nearest, directional-nearest, cost-function, VIP) — Strategy. */
public interface DispatchStrategy {
    Elevator select(List<Elevator> elevators, int floor, Direction direction);
}
