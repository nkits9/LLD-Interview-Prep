# **LLD Interview Prep – SDE 3**

**Contents:** 1\. Interview Process  |  2\. Design Knowledge  |  3\. Concurrency Deep-Dive  |  4\. Core 12 Problems (with must-cover checklists)  |  5\. Additional Problems  |  6\. Extension Questions  |  7\. Communication  |  8\. Mistakes That Fail Candidates  |  9\. Done-Checklist  |  10\. Resources

# **1\. The 60-Minute Interview Process**

| Time | Do | What's being judged |
| :---- | :---- | :---- |
| 0–7 min | Ask 5–8 clarifying questions. Write down 3–5 in-scope features. Explicitly say what's out of scope. | Can you scope, or do you just start coding? |
| 7–15 min | Nouns → classes, verbs → methods. Say relationships aloud ("Floor has many Spots, Spot holds zero or one Vehicle"). Sketch boxes with has-a / is-a arrows. Don't UML-perfect it. | Entity modelling |
| 15–20 min | Identify where change will come (new type, new rule, new channel). Only here do patterns go in: "Pricing will vary, so PricingStrategy interface." | Extensibility judgement |
| 20–50 min | Code in this order: **enums → models/entities → interfaces → strategy implementations → services → thin driver**. Get one working vertical slice before covering every feature. | Working, clean code |
| 50–60 min | Walk one flow end-to-end. Discuss concurrency ("two users at once → where's the lock?"). Handle the "now add X" extension. | Depth and adaptability |

Practice with a timer until this rhythm is automatic.

# **2\. Design Knowledge**

## **2.1 SOLID — how each shows up in code**

> * **SRP:** a Booking doesn't compute price or send emails; PricingService and NotificationService do.  
> * **OCP:** new vehicle type \= new class/enum entry, never editing an if-else chain.  
> * **LSP:** avoid Square-extends-Rectangle hierarchies; prefer interfaces.  
> * **ISP:** small interfaces (Payable, Cancellable) over one fat Order interface.  
> * **DIP:** services depend on PaymentGateway interface; concrete RazorpayGateway is injected.  
> * Composition over inheritance; interfaces vs abstract classes — know when each.

## **2.2 Design patterns — know the problem each solves; write each from memory in under 3 minutes**

| Pattern | Solves | Use cases |
| :---- | :---- | :---- |
| **Strategy** | A rule/algorithm that varies | Pricing, allocation, matching, payment, splitting. The single most-used pattern. |
| **State** | Behaviour differs by lifecycle stage; avoid giant switch | Vending machine, elevator, order lifecycle, booking, ATM |
| **Observer** | Notify many on a change | Order status, stock updates, event systems, notifications |
| **Factory / Abstract Factory** | Create objects by type without if-else | Vehicles, pieces, notification channels |
| **Builder** | Objects with many optional fields | Pizza, Query, User profile, Log config |
| **Singleton** | One shared instance | Managers/registries. Know the criticism and thread-safe forms (double-checked locking with volatile, eager init, enum, holder idiom). Don't use unless asked. |
| **Command** | Encapsulate an action; undo/redo | Text editor, remote control, task queues, ledgers |
| **Decorator** | Add behaviour by wrapping | Toppings; logging/caching/retry around a component |
| **Chain of Responsibility** | Pass request along handlers | Logger levels, approval workflows, request filters |
| **Composite** | Tree of uniform nodes | File system, org hierarchy, UI trees |
| **Memento** | Snapshot and restore | Undo, checkpoints |
| Template Method / Facade / Adapter / Repository / Visitor | — | Recognize and name; rarely drive a design |

## **2.3 Anti-patterns to name and avoid**

> * God class (one Manager doing everything)  
> * Anemic models paired with a fat service (acceptable only if intentional)  
> * String-typed status fields instead of enums  
> * Public mutable fields  
> * Inheritance for code reuse instead of true is-a  
> * Deep hierarchies (Vehicle → Car → SUV → LuxurySUV)  
> * Pattern stuffing — Factory \+ Singleton \+ Observer where a plain class would do

## **2.4 Class design hygiene**

> * Immutable value objects (Money, Address, Coordinates).  
> * Never double for money — integer minor units or BigDecimal.  
> * Typed or clearly named IDs, generated in one place.  
> * Return unmodifiable collection views.  
> * Validate at construction; invalid state should be unrepresentable.  
> * In-memory repositories as Map\<Id, Entity\>, not lists.  
> * Custom exceptions (SpotUnavailableException) over generic ones.  
> * Inject a clock; never read system time inline (testability).  
> * Enums with behaviour/ordering (VehicleSize.canFit(SpotSize)) instead of if-else on type.

# **3\. Concurrency Deep-Dive — the SDE 2 vs SDE 3 differentiator**

"What if two users do this at the same time?" is asked on nearly every problem. The bar: name the shared state, show the critical section, pick a lock granularity, explain the trade-off, and know what you'd reach for if the naive lock doesn't scale.

## **3.1 Memory model fundamentals**

> * **Atomicity, visibility, ordering** — the three problems concurrency primitives solve. A race can be any of them, not just "two writes."  
> * **Happens-before**: synchronized, volatile, lock acquire/release, thread start/join all establish it. Without it a reader may never see a write.  
> * volatile \= visibility \+ ordering, **not** atomicity (count++ on a volatile is still a race). Why double-checked locking needs volatile.  
> * Immutable objects are safely shared without synchronization — the strongest argument for value objects.  
> * Know your language's equivalents (Java Memory Model, Go memory model, Python GIL implications).

## **3.2 Primitives — know what each is for and one place you'd use it**

| Primitive | Purpose | Where in the 12 problems |
| :---- | :---- | :---- |
| Mutex / synchronized | Simple mutual exclusion | Small critical sections everywhere |
| ReentrantLock | Mutex with tryLock, timeout, fairness, multiple conditions | Parking spot acquire with timeout |
| ReadWriteLock / StampedLock | Many readers, few writers; optimistic reads | File System directory listing vs create |
| Semaphore | Bounded access to N resources | Elevator capacity, restaurant concurrent-order cap, connection pools |
| Condition variables (wait/notify, Condition) | Wait until a predicate holds; always in a loop | Bounded queue in Logger (not-full / not-empty) |
| CountDownLatch / CyclicBarrier | One-shot / reusable rendezvous | Wait for N warehouses to confirm stock before committing |
| Atomics (AtomicInteger, AtomicReference, LongAdder) | Lock-free single-variable updates | Rate Limiter token count, stock counters |
| Concurrent collections | ConcurrentHashMap (compute, putIfAbsent, merge), ConcurrentSkipListMap, CopyOnWriteArrayList, blocking queues | Per-key state maps, observer lists, request queues |
| ThreadLocal | Per-thread state, no sharing | Request context in Logger (trace id) |

## **3.3 Lock-free and CAS**

> * **Compare-and-swap**: read, compute, CAS; retry on failure. Basis of all atomics.  
> * Pattern: do { old \= ref.get(); new \= f(old); } while (\!ref.compareAndSet(old, new));  
> * **ABA problem** and why versioned/stamped references exist.  
> * When lock-free wins (short critical sections, high contention on a single variable) and when it doesn't (multi-variable invariants — use a lock).  
> * Rate Limiter token bucket as a CAS loop is a strong interview answer.

## **3.4 Lock granularity and strategy**

> * **Coarse** (one lock for the lot) — simple, correct, serializes everything.  
> * **Fine-grained** (one lock per spot/seat) — throughput, but multi-resource operations need ordering.  
> * **Lock striping** — N locks, resource hashes to a stripe. How ConcurrentHashMap historically worked. Use for per-client buckets in Rate Limiter or per-key inventory.  
> * **Lock ordering** — always acquire in a global order (by ID) to prevent deadlock when locking several seats.  
> * **tryLock with timeout** over blocking lock in user-facing paths; fail fast, let the caller retry.  
> * Minimize the critical section: compute outside, mutate inside. Never do I/O (payment call) while holding a lock.  
> * Prefer compute/merge on concurrent maps over check-then-act (which is always a race).

## **3.5 Transactional thinking in LLD**

> * **Two-phase locking (2PL)** as a mental model: growing phase (acquire every lock you'll need), shrinking phase (release only after all mutations). Guarantees serializability of a multi-resource operation. In LLD: booking 3 seats — lock all three (in ID order), validate all, mutate all, release all. Never lock-mutate-release one seat at a time. *Strict 2PL* \= hold all until commit. Know it's a DBMS concept — you describe it, you don't implement a lock manager.  
> * **Pessimistic locking** — lock before read; safe under high contention, lower throughput, deadlock risk.  
> * **Optimistic locking** — read with version, write only if version unchanged, retry on conflict. Best under low contention. Implement as a version field on the entity checked in the update.  
> * **MVCC idea** — readers see a snapshot, writers create new versions; readers never block writers. Why databases prefer it; how CopyOnWriteArrayList is the same idea in miniature.  
> * **Reserve-with-TTL** — hold → confirm-or-expire. The practical way to avoid holding locks across user think-time or external calls (seats, carts, lockers).  
> * **Idempotency keys** — the same request twice yields the same result once. Store request-id → result. Essential wherever retries exist (payments, order placement).  
> * **Compensating actions / Saga** — for multi-step flows that can't be one atomic unit: hold seats → charge payment → confirm; on payment failure release holds; on confirm failure refund. Each step has an undo.  
> * **Isolation levels** (for the "what if this were backed by a DB?" follow-up): Read Uncommitted (dirty reads), Read Committed (non-repeatable reads), Repeatable Read (phantoms), Serializable. Know which anomaly each allows and that Postgres/MySQL default to Read Committed / Repeatable Read respectively.  
> * **Event sourcing / append-only ledger** — every change is an immutable event; current state is derived. Natural fit for Inventory and payments; makes audit and replay free.

## **3.6 Deadlock, livelock, starvation**

> * Four conditions for deadlock: mutual exclusion, hold-and-wait, no preemption, circular wait. Break any one.  
> * Prevention: global lock ordering (most practical), acquire-all-or-none with tryLock, lock timeouts.  
> * Detection: wait-for graph, cycle → abort a victim. What databases do; what you name, not implement.  
> * Livelock: threads keep retrying and yielding to each other; add randomized backoff.  
> * Starvation: unfair locks; use fair locks or bounded queues when it matters.

## **3.7 Asynchrony and thread pools**

> * **Producer-consumer** with a bounded blocking queue: the backbone of Logger, Elevator dispatch, Task Scheduler, Notification service.  
> * Bounded, never unbounded queues. **Rejection policy** when full: block caller (backpressure), drop oldest/newest, spill to disk, fail fast.  
> * Thread pool sizing: CPU-bound ≈ cores; I/O-bound ≈ cores × (1 \+ wait/compute). Separate pools for separate concerns so one slow sink can't starve others.  
> * Futures / CompletableFuture / promises for fan-out (notify via email, SMS, push in parallel) and timeouts.  
> * Scheduled executors for TTL expiry sweeps; prefer lazy expiry (check on access) where possible — no per-key timers.  
> * Graceful shutdown: stop accepting, drain, flush, then exit. Say it on every async design.  
> * Retry with exponential backoff \+ jitter; circuit breaker around flaky external calls.

## **3.8 Confinement and immutability — the first line of defence**

> * Best concurrency is no shared mutable state. Ask first: can this be immutable? Thread-confined? Passed by message?  
> * Immutable value objects and copy-on-write snapshots.  
> * Thread confinement: ThreadLocal, single-threaded event loop per resource (actor model — one elevator, one thread, one queue).  
> * Message passing between components over sharing objects.

## **3.9 Which tool when — decision table**

| Situation | Reach for |
| :---- | :---- |
| One counter / flag / reference | Atomic (CAS) |
| Per-key state, many keys | ConcurrentHashMap.compute or lock striping |
| Read-heavy, rare writes | ReadWriteLock, or copy-on-write / immutable snapshot |
| Multi-field invariant on one object | Lock on that object (fine-grained) |
| Operation spans several objects | Lock all in global order (2PL discipline) or optimistic versions \+ retry |
| Hold across user think-time or external call | Never hold a lock — reserve-with-TTL \+ idempotent confirm |
| Multi-step with external side effects | Saga with compensating actions |
| Work handed off between components | Bounded blocking queue \+ worker pool |
| Limit N concurrent users of a resource | Semaphore |
| Wait until a condition holds | Condition variable in a loop |
| Need audit / replay | Append-only event log |

## **3.10 Concurrency mapped to the 12 problems**

| Problem | Concept it exercises |
| :---- | :---- |
| Amazon Locker | Per-locker lock, compute on map, TTL expiry sweep vs lazy expiry |
| Parking Lot | Fine-grained lock / AtomicBoolean per spot, tryLock with timeout |
| Elevator | Bounded queue per elevator, actor-style single thread per car, semaphore for capacity |
| File System | ReadWriteLock per directory, lock ordering on move (parent → child) |
| Movie Ticket Booking | 2PL discipline over multiple seats, reserve-with-TTL, idempotent confirm, optimistic vs pessimistic, saga on payment failure |
| Logging Service | Producer-consumer, condition variables, rejection policy, graceful shutdown, separate pool per sink |
| Rate Limiter | CAS loop for token bucket, lock striping per client, clock injection, lazy refill |
| Inventory Management | Optimistic versioning, reserved vs committed counts, event-sourced ledger, isolation-level follow-up |
| Splitwise | Per-group lock, immutable Money, deterministic recomputation |
| Food Delivery | CAS on order assignment (two riders accept), state-machine transitions under lock, timeouts via scheduler |
| Text Editor | Mostly single-threaded; discuss what breaks under collaboration (OT/CRDT by name) |

# **4\. Core 12 Problems — Solve in This Order**

Each introduces one or two new concepts and reuses earlier ones. **Must cover** items are what a happy-case solution skips — you are not done until each is in code, not just discussed.

## **1\. Connect Four — warm-up, learn the process**

> * **Patterns:** none forced. Separate Board (state), Game (loop/turns), WinChecker (rules). Player as a class, not a string.  
> * **Must cover:** invalid move (full column, out of bounds) as exceptions; draw detection; win check only around the last placed piece; configurable board size and win-length.  
> * **Extension:** swap to Tic-Tac-Toe with minimal changes → rules behind an interface.

## **2\. Amazon Locker — allocation \+ expiry**

> * **Patterns:** Strategy (locker selection: smallest-fit, nearest); Enum for sizes with ordering.  
> * **Concurrency:** two packages assigned the same locker — lock per locker or compute on a concurrent map; never lock the whole location.  
> * **Must cover:** package too large for any locker; OTP generation and single-use validation; unclaimed-package expiry with a scheduled sweep; returning a locker to the pool atomically.  
> * **Extension:** multiple locations, reservations before delivery.

## **3\. Parking Lot — the template problem**

> * **Patterns:** Strategy (spot allocation, pricing), Factory (vehicle by type), Repository for spots.  
> * **Concurrency:** parallel entry gates — fine-grained lock or AtomicBoolean per spot; tryLock with timeout, not a blocking lock.  
> * **Must cover:** lot full → clean failure; lost ticket on exit; time-based pricing with rounding rules; money as integer minor units; spot compatibility matrix (smaller vehicles fit bigger spots), not if-else.  
> * **Extension:** EV charging spots, monthly pass holders → new classes/strategies only.

## **4\. Elevator — State \+ scheduling**

> * **Patterns:** State (Idle, MovingUp, MovingDown, DoorsOpen, Maintenance); Strategy (dispatch: nearest-car, SCAN/LOOK); Observer (display panels).  
> * **Concurrency:** bounded blocking request queue per elevator; dispatcher thread; requests arriving while doors are closing.  
> * **Must cover:** duplicate requests for the same floor; overload/weight limit; direction change logic (finish all up requests before reversing); emergency stop from any state; N elevators, not one.  
> * **Extension:** prioritize a VIP floor → new strategy.

## **5\. File System (in-memory) — Composite \+ path handling**

> * **Patterns:** Composite (File and Directory share a node interface); Visitor or recursive traversal for size/search.  
> * **Concurrency:** concurrent create in the same directory — lock per directory; reads don't block reads (ReadWriteLock).  
> * **Must cover:** path parsing (.., ., trailing slash); duplicate names; deleting a non-empty directory; move/rename across directories; permissions as an add-on; cycle prevention on move (can't move a dir into its own child).  
> * **Extension:** symlinks, quotas.

## **6\. Movie Ticket Booking — concurrency centrepiece**

> * **Patterns:** State (seat: Available → Held → Booked; booking: Created → Paid → Confirmed/Cancelled); Strategy (pricing by tier/time); Observer (email/SMS on confirm).  
> * **Concurrency:** hold seats with TTL, auto-release on timeout; lock all requested seats in ID order (2PL discipline); idempotent confirm (payment retry must not double-book); optimistic version check vs pessimistic per-seat lock — argue both; payment failure triggers compensating release (saga).  
> * **Must cover:** partial availability (want 3 adjacent, only 2 free) → all-or-nothing; payment failure rolls back holds; cancellation and refund rules; same user double-clicking.  
> * **Extension:** multiple screens/shows per theatre; group booking with adjacency preference.

## **7\. Logging Service — Chain of Responsibility \+ async**

> * **Patterns:** Chain of Responsibility (levels), Strategy (formatters), Observer/Strategy (sinks: console, file, remote), Builder (config), Decorator (timestamp/thread-id).  
> * **Concurrency:** producer-consumer with a bounded queue and worker thread; full queue → block, drop, or overflow to disk; graceful shutdown that flushes.  
> * **Must cover:** level filtering per sink; file rotation by size; a failing sink must not break others; thread-safe global access without a hard Singleton.  
> * **Extension:** structured JSON logs, per-module log levels.

## **8\. Rate Limiter — algorithms \+ per-key state**

> * **Patterns:** Strategy (fixed window, sliding window log, sliding window counter, token bucket, leaky bucket — implement at least three); Factory to pick by config.  
> * **Concurrency:** per-client bucket in a concurrent map; lazy refill on access, not a timer per key; atomic check-and-decrement via CAS loop.  
> * **Must cover:** clock injection for testability; burst vs sustained rate; cleanup of idle client state; 429 with retry-after; different limits per tier.  
> * **Extension:** distributed version — describe what changes (shared store, atomic ops), don't code it.

## **9\. Inventory Management — transactions \+ consistency**

> * **Patterns:** Repository, Strategy (reservation/restock policies), Observer (low-stock alerts), Command (stock movements as an append-only ledger).  
> * **Concurrency:** reserve-and-decrement atomic; oversell prevention; optimistic locking with version \+ reconciliation on failure.  
> * **Must cover:** reserved vs committed stock (two numbers, not one); multi-warehouse allocation; returns and damaged goods as separate movement types; audit trail — every change is an event, current stock is derived; batch/expiry tracking.  
> * **Extension:** backorders, transfers between warehouses.

## **10\. Splitwise — money correctness \+ validation**

> * **Patterns:** Strategy (equal / exact / percentage / share splits), Factory for split type.  
> * **Concurrency:** two users adding to the same group — lock per group.  
> * **Must cover:** splits must sum to total — validate before mutating; deterministic rounding (₹100 ÷ 3 — who gets the extra paisa); balance simplification as a graph problem (min transactions); groups vs one-off expenses; partial settle-up; expense edit/delete recomputing balances.

## **11\. Food Delivery (Swiggy-lite) — event-driven lifecycle**

> * **Patterns:** State (Placed → Accepted → Preparing → PickedUp → Delivered / Cancelled, allowed transitions in one place); Strategy (rider matching, delivery fee); Observer (customer, restaurant, rider notified differently); Factory (notifications).  
> * **Concurrency:** two riders accepting the same order (CAS on assignment); restaurant capacity limits (semaphore).  
> * **Must cover:** cancellation rules per state (free before accept, charged after); rider unassignment/reassignment; timeouts (restaurant doesn't accept in N min); refunds; illegal transitions rejected explicitly.  
> * **Extension:** scheduled orders, multi-restaurant carts.

## **12\. Text Editor with Undo/Redo — Command \+ Memento**

> * **Patterns:** Command (insert, delete, replace with execute/undo), two stacks; Memento for snapshots; Composite command for macros.  
> * **Must cover:** redo stack cleared on new edit; grouping consecutive keystrokes into one undo unit; cursor position as part of state; large-document efficiency (piece table / gap buffer — know the name, implement simply).  
> * **Extension:** collaborative editing — say what breaks (name OT / CRDT).

## **Concept coverage map**

| Concept | Taught by |
| :---- | :---- |
| Strategy | Locker, Parking Lot, Elevator, Rate Limiter, Splitwise, Food Delivery |
| State | Elevator, Movie Booking, Food Delivery |
| Observer | Elevator, Movie Booking, Logging, Inventory, Food Delivery |
| Factory | Parking Lot, Rate Limiter, Splitwise, Food Delivery |
| Composite | File System, Text Editor |
| Command / Memento | Inventory (ledger), Text Editor |
| Chain of Responsibility / Decorator / Builder | Logging Service |
| Fine-grained locking / striping | Locker, Parking Lot, File System, Splitwise, Rate Limiter |
| 2PL discipline, reserve-with-TTL, idempotency, saga | Movie Booking, Inventory |
| Optimistic vs pessimistic locking | Movie Booking, Inventory |
| CAS / lock-free | Rate Limiter, Food Delivery assignment |
| Producer-consumer / bounded queues / shutdown | Elevator, Logging Service |
| Clock injection, lazy computation | Rate Limiter, Locker expiry |
| Money & rounding | Parking Lot, Splitwise |
| Event sourcing / audit trail | Inventory |

# **5\. Additional Problems (after the core 12; design fully, code at least partially)**

| Problem | Key ideas |
| :---- | :---- |
| Stock exchange / order matching engine | Price-time priority, order book with heaps/TreeMap, single-threaded matching per symbol, partial fills |
| Hotel booking | Overlapping date ranges, inventory per room type, concurrency |
| Pub-sub / message queue | Topics, consumer groups, offsets, at-least-once delivery |
| Meeting room scheduler | Interval conflicts, allocation |
| Task scheduler / cron | Priority queue, worker pool, retries, idempotent execution |
| Cab booking (Uber lite) | Matching Strategy, trip State, surge pricing |
| Vending machine / ATM / Coffee machine | Canonical State problem; change/refund handling |
| Chess | Composition vs inheritance for pieces, move validation, Command for undo, check/checkmate |
| LRU / LFU cache (thread-safe, TTL) | Data structure \+ eviction Strategy \+ locking / striping |
| Notification service | Observer \+ Factory \+ retry with backoff \+ Decorator \+ fan-out futures |
| Library management | CRUD, Repository, reservations, fines |
| Snake & Ladder / Tic-Tac-Toe | Game loop, rules separated from board |
| Poker / Blackjack / Monopoly | Many entities, rule engines |
| Leaderboard | Sorted structures, tie-breaking, frequent updates |
| Pizza / Coffee ordering | Builder \+ Decorator |
| Payment gateway / wallet | Idempotency, ledger, state machine, reconciliation, saga |
| Connection pool / object pool | Semaphore, blocking acquire with timeout, health checks |

# **6\. Extension Questions — pre-think for every problem**

The SDE 3 bar: your design must absorb each of these with minimal edits. If it can't, redesign.

> * Add a new type (vehicle/piece/split/channel) → one new class only.  
> * Change a rule (pricing, allocation, matching) → swap a strategy.  
> * Multi-tenant / multi-location → where does the extra key go?  
> * Persist it → which classes become repositories; what changes; which isolation level?  
> * Two concurrent requests → where's the lock, how small, what if it doesn't scale?  
> * Make it observable / auditable → Observer or event log hook.  
> * Make it distributed → what breaks (locks, clocks, idempotency)? Name the fix, don't code it.

# **7\. Communication Rules**

> * Narrate decisions as trade-offs: "Interface here because X will vary; if it won't, an enum is simpler."  
> * On pushback, don't defend reflexively: "That's fair — the alternative is Y, and I'd pick it when…"  
> * Code in your real language with real syntax; pseudocode reads as weakness at this level.  
> * Never say "in production I'd do it differently" without saying how.  
> * Leave TODO comments for skipped items and say them aloud.  
> * For every pattern and every lock, one sentence on why it's there — and why not elsewhere.

# **8\. Mistakes That Fail SDE 3 Candidates**

> * Jumping to code without scoping.  
> * Pattern stuffing — actively penalized at this level.  
> * Deep inheritance hierarchies.  
> * Not finishing a runnable flow — 80% coded beats 100% designed with nothing running.  
> * Hand-waving concurrency ("I'd use a lock") without saying where or how granular.  
> * Check-then-act on shared state (the classic race).  
> * Holding a lock across an external call or user think-time.  
> * Business logic in the driver/main.  
> * Freezing on the extension question.  
> * Only demonstrating the happy path.

# **9\. Done-Checklist — apply before calling any problem complete**

| ✓ | Check |
| :---- | :---- |
| ☐ | Two clients hit the same resource simultaneously: where's the lock, and how small is it? |
| ☐ | Any operation touching multiple resources acquires locks in a global order or uses optimistic versions. |
| ☐ | No lock is held across I/O or user think-time; reserve-with-TTL used instead. |
| ☐ | Retried requests are idempotent. |
| ☐ | Every invalid input and illegal state transition raises a specific exception. |
| ☐ | Money is never a double; time is never read directly (inject a clock). |
| ☐ | Adding a new type or rule requires zero edits to existing classes. |
| ☐ | One runnable driver exercises a non-happy path (failure, timeout, rollback), not just success. |
| ☐ | One sentence explaining why each pattern is there — and why you didn't use one elsewhere. |
| ☐ | All extension questions from Section 6 answered. |

# **10\. Resources (only these)**

> * refactoring.guru — pattern reference  
> * Hello Interview LLD problem set — matches the core list  
> * Ashish Pratap Singh's awesome-low-level-design (GitHub) — solved problems  
> * Head First Design Patterns — skim only  
> * Java Concurrency in Practice (chapters 1–5, 10–11, 13–15) or your language's equivalent — for Section 3  
> * Designing Data-Intensive Applications, chapter 7 (Transactions) — for 2PL, MVCC, isolation levels