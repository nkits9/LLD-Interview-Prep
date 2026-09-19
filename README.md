# LLD Interview Prep

Low-Level Design preparation for SDE3 interviews — design patterns and solved design problems in Java, each paired with a short, interview-centric revision doc (diagram + key snippets + expected questions). Built on the 80/20 principle: only what interviews actually test, concise enough to revise the night before.

**Source roadmap:** [LLD.md](LLD.md)

## ⚡ Pre-interview revision (~2 hours)

| Step | Read | Time |
|------|------|------|
| 1 | [Interview playbook](docs/interview-playbook.md) — the 60-min rhythm, communication rules, fatal mistakes | 15 min |
| 2 | [Patterns cheatsheet](docs/patterns-cheatsheet.md) — what each solves, when NOT to use it | 15 min |
| 3 | [Concurrency cheatsheet](docs/concurrency-cheatsheet.md) — locks, granularity, which-tool-when | 30 min |
| 4 | [SOLID & class hygiene](docs/solid-and-hygiene.md) — quick scan | 10 min |
| 5 | READMEs of the 3–4 most likely problems below — diagram + must-cover list + follow-up Q&A | 50 min |

## 📦 Design patterns (goal: write each from memory in under 3 minutes)

| Pattern | Solves | Status |
|---------|--------|:------:|
| [Strategy](src/patterns/behavioral/strategy/) | A rule/algorithm that varies — pricing, allocation, matching, splitting | ✅ |
| [State](src/patterns/behavioral/state/) | Behaviour differs by lifecycle stage; kills the giant switch | ✅ |
| [Observer](src/patterns/behavioral/observer/) | Notify many on a change | ✅ |
| [Factory](src/patterns/creational/factory/) | Create objects by type without if-else (incl. Abstract Factory) | ✅ |
| [Builder](src/patterns/creational/builder/) | Objects with many optional fields | ✅ |
| [Singleton](src/patterns/creational/singleton/) | One shared instance — know the thread-safe forms and the criticism | ✅ |
| [Command](src/patterns/behavioral/command/) | Encapsulate an action; undo/redo | ✅ |
| [Decorator](src/patterns/structural/decorator/) | Add behaviour by wrapping | ✅ |
| [Chain of Responsibility](src/patterns/behavioral/chainofresponsibility/) | Pass a request along handlers | ✅ |
| [Composite](src/patterns/structural/composite/) | Tree of uniform nodes | ✅ |
| [Memento](src/patterns/behavioral/memento/) | Snapshot and restore | ✅ |

Recognize-only (no code, by design): Template Method, Facade, Adapter, Visitor → [one-liners in the cheatsheet](docs/patterns-cheatsheet.md#recognize-only-patterns).

## 🧩 Core 12 problems (solve in this order)

| # | Problem | New concepts it teaches | Status |
|---|---------|------------------------|:------:|
| 1 | [Connect Four](src/problems/p01_connectfour/) | The interview process itself; rules behind an interface | ✅ |
| 2 | [Amazon Locker](src/problems/p02_amazonlocker/) | Allocation Strategy, TTL expiry, per-locker locking | ✅ |
| 3 | [Parking Lot](src/problems/p03_parkinglot/) | The template problem — Strategy + Factory, money, fine-grained locks | ✅ |
| 4 | [Elevator](src/problems/p04_elevator/) | State machine, dispatch Strategy, bounded queues per car | ✅ |
| 5 | [File System](src/problems/p05_filesystem/) | Composite, path handling, ReadWriteLock, lock ordering on move | ✅ |
| 6 | [Movie Ticket Booking](src/problems/p06_movieticketbooking/) | Concurrency centrepiece — 2PL, reserve-with-TTL, idempotency, saga | ✅ |
| 7 | [Logging Service](src/problems/p07_loggingservice/) | Chain of Responsibility, producer-consumer, graceful shutdown | ✅ |
| 8 | [Rate Limiter](src/problems/p08_ratelimiter/) | Limiting algorithms, CAS loops, lock striping, clock injection | ✅ |
| 9 | [Inventory Management](src/problems/p09_inventory/) | Optimistic locking, reserved-vs-committed stock, event ledger | ✅ |
| 10 | [Splitwise](src/problems/p10_splitwise/) | Money correctness, deterministic rounding, debt simplification | ✅ |
| 11 | [Food Delivery](src/problems/p11_fooddelivery/) | Event-driven lifecycle, CAS assignment, timeouts, refunds | ✅ |
| 12 | [Text Editor](src/problems/p12_texteditor/) | Command + Memento, undo/redo stacks, macro commands | ✅ |

## ➕ Additional problems (after the core 12 — design fully, code at least partially)

| # | Problem | Key ideas | Status |
|---|---------|-----------|:------:|
| 1 | [Vending Machine](src/problems/extras/vendingmachine/) | The canonical State problem; change/refund handling | ✅ |
| 2 | [LRU / LFU Cache](src/problems/extras/lrucache/) | Data structure + eviction Strategy + locking/striping, TTL | ✅ |
| 3 | [Notification Service](src/problems/extras/notificationservice/) | Observer + Factory + Decorator, retry with backoff, fan-out futures | ✅ |
| 4 | [Task Scheduler / Cron](src/problems/extras/taskscheduler/) | Priority queue, worker pool, retries, idempotent execution | ✅ |
| 5 | [Meeting Room Scheduler](src/problems/extras/meetingscheduler/) | Interval conflicts, allocation | ✅ |
| 6 | [Hotel Booking](src/problems/extras/hotelbooking/) | Overlapping date ranges, inventory per room type, concurrency | ✅ |
| 7 | [Cab Booking (Uber-lite)](src/problems/extras/cabbooking/) | Matching Strategy, trip State, surge pricing | ✅ |
| 8 | [IRCTC Train Booking](src/problems/extras/irctc/) | Seat×segment inventory (bitmask), seat reuse across intermediate stations, fair per-run lock | ✅ |
| 9 | [Payment Gateway / Wallet](src/problems/extras/paymentgateway/) | Idempotency, ledger, state machine, reconciliation, saga | ✅ |
| 10 | [Connection / Object Pool](src/problems/extras/connectionpool/) | Semaphore, blocking acquire with timeout, health checks | ✅ |
| 11 | [Pub-Sub / Message Queue](src/problems/extras/pubsub/) | Topics, consumer groups, offsets, at-least-once delivery | ✅ |
| 12 | [Stock Exchange / Order Matching](src/problems/extras/stockexchange/) | Price-time priority, order book (heaps/TreeMap), single-threaded matching per symbol, partial fills | ✅ |
| 13 | [Leaderboard](src/problems/extras/leaderboard/) | Sorted structures, tie-breaking, frequent updates | ✅ |
| 14 | [Chess](src/problems/extras/chess/) | Composition vs inheritance for pieces, move validation, Command for undo | ✅ |
| 15 | [Snake & Ladder / Tic-Tac-Toe](src/problems/extras/snakeladder/) | Game loop, rules separated from board | ✅ |
| 16 | [Pizza / Coffee Ordering](src/problems/extras/pizzaordering/) | Builder + Decorator | ☐ |
| 17 | [Library Management](src/problems/extras/librarymanagement/) | CRUD, Repository, reservations, fines | ☐ |
| 18 | [Poker / Blackjack / Monopoly](src/problems/extras/cardgames/) | Many entities, rule engines | ☐ |

Details in [LLD.md §5](LLD.md). Ordered roughly by interview frequency and concept payoff — the first ~9 are the highest-value after the core 12.

## Running a demo

Open the repo in IntelliJ (`src` is the sources root) and run any `Demo.java`. Or from the terminal:

```bash
cd src
javac $(find problems/p03_parkinglot -name '*.java') && java problems.p03_parkinglot.Demo
```

## Repo conventions

- Every pattern/problem folder is self-contained: `README.md` (the revision doc) + code + runnable `Demo.java`.
- New revision docs are copied from [templates/](templates/) so every doc has the same shape.
- Every `Demo` exercises one happy path **and one failure path** (per the [done-checklist](docs/interview-playbook.md#done-checklist--before-calling-any-problem-complete)).
- No frameworks, no dependencies — plain Java, exactly what you'd write in an interview.
- Adding a new pattern/problem/concept? The workflow, structure, and quality bar are encoded as a Claude Code skill: [.claude/skills/add-lld-item/SKILL.md](.claude/skills/add-lld-item/SKILL.md) — auto-loaded in any Claude Code session in this repo.
