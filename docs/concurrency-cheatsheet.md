# Concurrency Cheatsheet

The SDE2 vs SDE3 differentiator. "What if two users do this at the same time?" is asked on nearly every problem. The bar: **name the shared state, show the critical section, pick a lock granularity, explain the trade-off, and know what you'd reach for if the naive lock doesn't scale.**

## Fundamentals (say these correctly)

- Concurrency primitives solve three problems: **atomicity, visibility, ordering**. A race can be any of them.
- **Happens-before** is established by `synchronized`, `volatile`, lock acquire/release, thread start/join. Without it a reader may never see a write.
- `volatile` = visibility + ordering, **not** atomicity (`count++` on a volatile is still a race). This is why double-checked locking needs volatile.
- **Immutable objects are safely shared with no synchronization** — the strongest argument for value objects.
- First line of defence is *no shared mutable state*: immutability, thread confinement (ThreadLocal, one thread per resource — actor style), message passing over shared objects.

## Primitives — what each is for

| Primitive | Purpose | Example use here |
|-----------|---------|------------------|
| `synchronized` / mutex | Simple mutual exclusion | Small critical sections everywhere |
| `ReentrantLock` | tryLock, timeout, fairness, multiple conditions | Parking spot acquire with timeout |
| `ReadWriteLock` / `StampedLock` | Many readers, few writers | File System: list vs create in a directory |
| `Semaphore` | Bound access to N resources | Elevator capacity, connection pools |
| Condition vars (`wait/notify`, `Condition`) | Wait until a predicate holds — **always in a loop** | Bounded queue in Logger (not-full / not-empty) |
| `CountDownLatch` / `CyclicBarrier` | One-shot / reusable rendezvous | Wait for N warehouses to confirm |
| Atomics (`AtomicInteger`, `AtomicReference`, `LongAdder`) | Lock-free single-variable updates | Rate Limiter token count |
| `ConcurrentHashMap` (`compute`, `putIfAbsent`, `merge`) | Per-key state without a global lock | Per-client buckets, per-spot state |
| `ThreadLocal` | Per-thread state, no sharing | Trace id in Logger |

## Lock granularity ladder

Coarse (one lock for everything — simple, serializes all) → fine-grained (lock per spot/seat — throughput, but multi-resource ops need ordering) → **lock striping** (N locks, key hashes to a stripe) → lock-free (CAS).

Rules that score points:

- **Lock ordering**: acquire multiple locks in a global order (by ID) to prevent deadlock.
- **tryLock with timeout** over blocking lock in user-facing paths — fail fast, caller retries.
- Minimize the critical section: compute outside, mutate inside. **Never do I/O (payment call) while holding a lock.**
- Prefer `map.compute/merge` over check-then-act — check-then-act on shared state is always a race.

## CAS / lock-free

```java
do { old = ref.get(); next = f(old); } while (!ref.compareAndSet(old, next));
```

- Basis of all atomics. Know the **ABA problem** → versioned/stamped references.
- Lock-free wins on short critical sections with high contention on **one** variable. Multi-variable invariants → use a lock.
- Rate Limiter token bucket as a CAS loop is a strong interview answer.

## Transactional toolkit (multi-resource operations)

| Idea | One-liner |
|------|-----------|
| **2PL discipline** | Acquire *all* locks (in ID order), validate all, mutate all, release all. Never lock-mutate-release one seat at a time. Describe it; don't build a lock manager. |
| **Pessimistic locking** | Lock before read. Safe under high contention; lower throughput; deadlock risk. |
| **Optimistic locking** | Read with version; write only if version unchanged; retry on conflict. Best under low contention. A version field on the entity. |
| **MVCC** | Readers see a snapshot, writers make new versions; readers never block writers. `CopyOnWriteArrayList` is this in miniature. |
| **Reserve-with-TTL** | Hold → confirm-or-expire. The way to never hold a lock across user think-time or external calls (seats, carts, lockers). |
| **Idempotency keys** | Same request twice → same result once. Store request-id → result. Essential wherever retries exist. |
| **Saga / compensating actions** | Multi-step flow: hold seats → charge → confirm; each step has an undo (release, refund). |
| **Event sourcing** | Every change is an immutable event; state is derived. Free audit and replay (Inventory, payments). |
| **Isolation levels** | Read Uncommitted (dirty reads) → Read Committed (non-repeatable reads) → Repeatable Read (phantoms) → Serializable. Postgres defaults RC, MySQL RR. |

## Deadlock, livelock, starvation

- Deadlock needs all four: mutual exclusion, hold-and-wait, no preemption, circular wait — **break any one**.
- Prevention: global lock ordering (most practical), acquire-all-or-none with tryLock, lock timeouts.
- Detection (wait-for graph, abort a victim) is what databases do — name it, don't implement it.
- Livelock → randomized backoff. Starvation → fair locks / bounded queues.

## Async & thread pools

- **Producer-consumer with a bounded blocking queue** — backbone of Logger, Elevator dispatch, schedulers, notifications.
- Bounded, never unbounded. Rejection policy when full: block caller (backpressure), drop oldest/newest, spill to disk, fail fast — pick one aloud.
- Pool sizing: CPU-bound ≈ cores; I/O-bound ≈ cores × (1 + wait/compute). Separate pools per concern.
- Futures/`CompletableFuture` for fan-out (email + SMS + push in parallel) and timeouts.
- TTL expiry: prefer **lazy expiry** (check on access) over a timer per key; a single scheduled sweep otherwise.
- **Graceful shutdown**: stop accepting, drain, flush, exit — say it on every async design.
- Retry with exponential backoff + jitter; circuit breaker around flaky external calls.

## Which tool when

| Situation | Reach for |
|-----------|-----------|
| One counter / flag / reference | Atomic (CAS) |
| Per-key state, many keys | `ConcurrentHashMap.compute` or lock striping |
| Read-heavy, rare writes | ReadWriteLock, or copy-on-write / immutable snapshot |
| Multi-field invariant on one object | Lock on that object (fine-grained) |
| Operation spans several objects | Lock all in global order (2PL) or optimistic versions + retry |
| Hold across user think-time or external call | Never hold a lock — reserve-with-TTL + idempotent confirm |
| Multi-step with external side effects | Saga with compensating actions |
| Work handed off between components | Bounded blocking queue + worker pool |
| Limit N concurrent users of a resource | Semaphore |
| Wait until a condition holds | Condition variable in a loop |
| Need audit / replay | Append-only event log |

## Where each concept lives in the 12 problems

| Problem | Exercises |
|---------|-----------|
| Amazon Locker | Per-locker lock, `compute` on map, TTL expiry sweep vs lazy expiry |
| Parking Lot | Fine-grained lock / AtomicBoolean per spot, tryLock with timeout |
| Elevator | Bounded queue per elevator, actor-style single thread per car, semaphore capacity |
| File System | ReadWriteLock per directory, lock ordering on move (parent → child) |
| Movie Booking | 2PL over seats, reserve-with-TTL, idempotent confirm, optimistic vs pessimistic, saga |
| Logging Service | Producer-consumer, condition variables, rejection policy, graceful shutdown |
| Rate Limiter | CAS token bucket, lock striping per client, clock injection, lazy refill |
| Inventory | Optimistic versioning, reserved vs committed counts, event ledger, isolation levels |
| Splitwise | Per-group lock, immutable Money, deterministic recomputation |
| Food Delivery | CAS on order assignment, state transitions under lock, scheduler timeouts |
| Text Editor | Single-threaded; name what breaks under collaboration (OT / CRDT) |
