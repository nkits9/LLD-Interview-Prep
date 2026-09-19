package problems.p04_elevator;

/** Enum state machine — the honest State form here (per-state behaviour is small). */
public enum ElevatorState {
    IDLE, MOVING_UP, MOVING_DOWN, DOORS_OPEN, MAINTENANCE
}
