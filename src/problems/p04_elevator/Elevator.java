package problems.p04_elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * One car. Stops live in two sorted sets (above/below) — TreeSet gives LOOK
 * for free: keep moving while ceiling/floor queries find work ahead, reverse
 * only when nothing's left in the current direction. Duplicate requests
 * dedupe by set semantics.
 *
 * Tick-based: step() advances one floor (or one door cycle) per call. In a
 * threaded build each car would be a single actor thread draining a bounded
 * queue — same logic, same single-writer safety.
 */
public class Elevator {
    private static final int MAX_PENDING = 8; // bounded: reject, don't grow unbounded

    private final String id;
    private final int capacity;
    private final List<ElevatorObserver> observers = new CopyOnWriteArrayList<>();
    private final TreeSet<Integer> stops = new TreeSet<>();

    private int floor;
    private int passengers;
    private ElevatorState state = ElevatorState.IDLE;
    private Direction direction; // meaningful while moving / deciding

    public Elevator(String id, int startFloor, int capacity) {
        this.id = id;
        this.floor = startFloor;
        this.capacity = capacity;
    }

    public String id() {
        return id;
    }

    public int floor() {
        return floor;
    }

    public ElevatorState state() {
        return state;
    }

    public int pendingStops() {
        return stops.size();
    }

    public void subscribe(ElevatorObserver observer) {
        observers.add(observer);
    }

    /** Hall or car call. Set semantics dedupe repeated presses for the same floor. */
    public void requestStop(int target) {
        if (state == ElevatorState.MAINTENANCE) {
            throw new IllegalStateException("elevator " + id + " is out of service");
        }
        if (target == floor && state != ElevatorState.MOVING_UP && state != ElevatorState.MOVING_DOWN) {
            openDoors();
            return;
        }
        if (stops.size() >= MAX_PENDING && !stops.contains(target)) {
            throw new RequestQueueFullException(id, MAX_PENDING);
        }
        stops.add(target);
        if (state == ElevatorState.IDLE) {
            direction = target > floor ? Direction.UP : Direction.DOWN;
            state = target > floor ? ElevatorState.MOVING_UP : ElevatorState.MOVING_DOWN;
            notifyObservers();
        }
    }

    /** Overload check happens at boarding; doors stay open on rejection. */
    public void board(int count) {
        if (state != ElevatorState.DOORS_OPEN) {
            throw new IllegalStateException("doors are not open on " + id);
        }
        if (passengers + count > capacity) {
            throw new CapacityExceededException(
                    id + " over capacity: " + passengers + "+" + count + " > " + capacity);
        }
        passengers += count;
    }

    public void alight(int count) {
        passengers = Math.max(0, passengers - count);
    }

    /** From ANY state: stop, dump pending work, go dark until serviced. */
    public void emergencyStop() {
        stops.clear();
        state = ElevatorState.MAINTENANCE;
        notifyObservers();
    }

    /** One simulation tick — the whole state machine. */
    public void step() {
        switch (state) {
            case MAINTENANCE:
            case IDLE:
                return;
            case DOORS_OPEN:
                closeDoorsAndDecide();
                return;
            case MOVING_UP:
                floor++;
                arriveOrContinue();
                return;
            case MOVING_DOWN:
                floor--;
                arriveOrContinue();
        }
    }

    private void arriveOrContinue() {
        if (stops.remove(floor)) {
            openDoors();
        } else {
            notifyObservers();
        }
    }

    private void openDoors() {
        state = ElevatorState.DOORS_OPEN;
        notifyObservers();
    }

    /**
     * LOOK: keep the current direction while any stop lies ahead; reverse only
     * when none do; idle when no stops at all.
     */
    private void closeDoorsAndDecide() {
        if (stops.isEmpty()) {
            state = ElevatorState.IDLE;
        } else if (direction == Direction.UP && stops.ceiling(floor) != null) {
            state = ElevatorState.MOVING_UP;
        } else if (direction == Direction.DOWN && stops.floor(floor) != null) {
            state = ElevatorState.MOVING_DOWN;
        } else {
            direction = (direction == Direction.UP) ? Direction.DOWN : Direction.UP;
            state = (direction == Direction.UP) ? ElevatorState.MOVING_UP : ElevatorState.MOVING_DOWN;
        }
        notifyObservers();
    }

    private void notifyObservers() {
        for (ElevatorObserver observer : observers) {
            observer.onUpdate(id, floor, state);
        }
    }
}
