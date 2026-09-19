package problems.p04_elevator;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        Elevator a = new Elevator("E-A", 2, 4);
        Elevator b = new Elevator("E-B", 5, 4);
        ElevatorSystem system = new ElevatorSystem(List.of(a, b), new NearestElevatorDispatch());

        // Observer: a display panel tracking E-A (Observer pattern).
        a.subscribe((id, floor, state) -> System.out.println("[display] " + id + " floor=" + floor + " " + state));

        // LOOK: E-A at 2 gets UP stops 4,6 and a DOWN stop 0 —
        // it must finish ALL up work before reversing.
        system.carCall("E-A", 4);
        system.carCall("E-A", 6);
        int before = a.pendingStops();
        system.carCall("E-A", 6); // duplicate press
        System.out.println("duplicate press: pending " + before + " -> " + a.pendingStops());
        system.carCall("E-A", 0);

        int guard = 0;
        while (a.state() != ElevatorState.DOORS_OPEN && guard++ < 10) {
            a.step();
        }
        // Overload at the first stop: capacity 4, trying to board 5.
        try {
            a.board(5);
        } catch (CapacityExceededException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        a.board(2);
        guard = 0;
        while (a.state() != ElevatorState.IDLE && guard++ < 30) {
            a.step();
        }

        // Dispatch strategy: floor 4 is nearer to E-B (5) than E-A (0).
        Elevator chosen = system.hallCall(4, Direction.UP);
        System.out.println("hall call floor 4 -> dispatched " + chosen.id());

        // Emergency stop from a moving state; car goes dark, dispatch avoids it.
        b.step();
        b.emergencyStop();
        System.out.println("E-B emergency-stopped: state=" + b.state());
        System.out.println("hall call floor 4 -> dispatched " + system.hallCall(4, Direction.UP).id()
                + " (E-B out of service)");
        try {
            system.carCall("E-B", 7);
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Bounded pending queue: 9th distinct stop is refused (backpressure).
        for (int floor : new int[]{1, 2, 3, 5, 6, 7, 8}) {
            system.carCall("E-A", floor);
        }
        try {
            system.carCall("E-A", 9);
        } catch (RequestQueueFullException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
