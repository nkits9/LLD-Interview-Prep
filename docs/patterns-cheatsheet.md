# Patterns Cheatsheet

One page. What each pattern solves, when you'd reach for it, and when a plain class is the honest answer.

## Trigger → pattern

What the interviewer says → what you should think.

| You hear… | Reach for |
|-----------|-----------|
| "pricing / allocation / matching / splitting can vary" | **Strategy** |
| "behaves differently per lifecycle stage" (order, booking, elevator) | **State** |
| "notify X, Y, Z when this changes" | **Observer** |
| "create the right subtype from input, no if-else chains" | **Factory** |
| "many optional fields / telescoping constructor" | **Builder** |
| "one shared registry/manager" | **Singleton** (say the criticism too) |
| "undo / redo / action history / audit of actions" | **Command** (+ **Memento** for snapshots) |
| "add behaviour without touching the class" (toppings, retry, logging) | **Decorator** |
| "levels of handling / escalation / filters" | **Chain of Responsibility** |
| "tree where leaves and containers are treated alike" | **Composite** |

## The core 11 — implemented in this repo

| Pattern | Solves | Where it shows up in the 12 problems |
|---------|--------|--------------------------------------|
| [Strategy](../src/patterns/behavioral/strategy/) | A rule/algorithm that varies. **The single most-used pattern.** | Locker, Parking Lot, Elevator, Rate Limiter, Splitwise, Food Delivery |
| [State](../src/patterns/behavioral/state/) | Behaviour differs by lifecycle stage; avoids a giant switch | Elevator, Movie Booking, Food Delivery |
| [Observer](../src/patterns/behavioral/observer/) | Notify many on a change | Elevator, Movie Booking, Logging, Inventory, Food Delivery |
| [Factory](../src/patterns/creational/factory/) | Create objects by type without if-else | Parking Lot, Rate Limiter, Splitwise, Food Delivery |
| [Builder](../src/patterns/creational/builder/) | Objects with many optional fields | Logging config, Pizza, Query |
| [Singleton](../src/patterns/creational/singleton/) | One shared instance. Know thread-safe forms: double-checked locking + volatile, eager init, enum, holder idiom. **Don't use unless asked.** | Managers/registries |
| [Command](../src/patterns/behavioral/command/) | Encapsulate an action; undo/redo | Text Editor, Inventory ledger, task queues |
| [Decorator](../src/patterns/structural/decorator/) | Add behaviour by wrapping | Toppings; logging/caching/retry around a component |
| [Chain of Responsibility](../src/patterns/behavioral/chainofresponsibility/) | Pass a request along handlers | Logger levels, approval workflows, request filters |
| [Composite](../src/patterns/structural/composite/) | Tree of uniform nodes | File System, org hierarchy, UI trees |
| [Memento](../src/patterns/behavioral/memento/) | Snapshot and restore | Undo, checkpoints |

## Recognize-only patterns

Name them when you see them; they rarely drive a design. No implementations here — on purpose (80/20).

| Pattern | One-liner |
|---------|-----------|
| Template Method | Fixed algorithm skeleton in a base class; subclasses fill in steps. Prefer Strategy (composition) in interviews. |
| Facade | One simple entry point hiding a messy subsystem. |
| Adapter | Wrap an incompatible interface so it fits yours (e.g., third-party payment SDK behind your `PaymentGateway`). |
| Visitor | Add operations to a stable object structure without editing it; double dispatch. Name it for File System traversal. |
| Repository | Abstract storage behind an interface; in-memory `Map<Id, Entity>` in interviews. |

## Anti-pattern-stuffing rules

Pattern stuffing is **actively penalized** at SDE3. Before adding any pattern, one sentence: *what change does it absorb?* If you can't answer, don't add it.

- A rule that will never vary → plain method, not Strategy.
- Two subtypes, unlikely to grow → constructor or simple `switch`, not Factory.
- One listener → direct call, not Observer.
- Singleton "because it's a manager" → just inject one instance.
- Deep hierarchies (Vehicle → Car → SUV → LuxurySUV) → composition + enums with behaviour.
