package problems.p04_elevator;

/** Display panels, monitoring — notified on every state/floor change (Observer). */
@FunctionalInterface
public interface ElevatorObserver {
    void onUpdate(String elevatorId, int floor, ElevatorState state);
}
