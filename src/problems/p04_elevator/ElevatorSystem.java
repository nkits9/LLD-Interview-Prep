package problems.p04_elevator;

import java.util.List;

/** N elevators + a dispatch strategy. Hall calls route through dispatch; car calls go direct. */
public class ElevatorSystem {
    private final List<Elevator> elevators;
    private final DispatchStrategy dispatch;

    public ElevatorSystem(List<Elevator> elevators, DispatchStrategy dispatch) {
        this.elevators = List.copyOf(elevators);
        this.dispatch = dispatch;
    }

    /** Button on a floor: strategy picks the car. */
    public Elevator hallCall(int floor, Direction direction) {
        Elevator chosen = dispatch.select(elevators, floor, direction);
        chosen.requestStop(floor);
        return chosen;
    }

    /** Button inside a car. */
    public void carCall(String elevatorId, int floor) {
        byId(elevatorId).requestStop(floor);
    }

    public void stepAll() {
        elevators.forEach(Elevator::step);
    }

    public Elevator byId(String id) {
        return elevators.stream().filter(e -> e.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no elevator " + id));
    }
}
