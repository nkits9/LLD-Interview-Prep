# SOLID & Class Design Hygiene

How each principle shows up **in code** (not as definitions), the anti-patterns to name and avoid, and the hygiene habits that make code read senior.

## SOLID — in code, not in theory

| Principle | How it looks in an LLD answer |
|-----------|-------------------------------|
| **SRP** | A `Booking` doesn't compute price or send emails; `PricingService` and `NotificationService` do. |
| **OCP** | New vehicle type = new class/enum entry, never editing an if-else chain. |
| **LSP** | Avoid Square-extends-Rectangle hierarchies; prefer interfaces. |
| **ISP** | Small interfaces (`Payable`, `Cancellable`) over one fat `Order` interface. |
| **DIP** | Services depend on a `PaymentGateway` interface; concrete `RazorpayGateway` is injected. |

Plus: **composition over inheritance**; know when an interface vs an abstract class (interface = contract, abstract class = shared partial implementation).

## Anti-patterns — name them to show judgement

- God class (one `Manager` doing everything).
- Anemic models + fat service (acceptable only if intentional — say so).
- String-typed status fields instead of enums.
- Public mutable fields.
- Inheritance for code reuse instead of true is-a.
- Deep hierarchies (Vehicle → Car → SUV → LuxurySUV).
- Pattern stuffing — Factory + Singleton + Observer where a plain class would do.

## Class design hygiene — free points in every problem

- Immutable value objects (`Money`, `Address`, `Coordinates`).
- **Never `double` for money** — integer minor units or `BigDecimal`.
- Typed or clearly named IDs, generated in one place.
- Return unmodifiable collection views.
- Validate at construction; invalid state should be unrepresentable.
- In-memory repositories as `Map<Id, Entity>`, not lists.
- Custom exceptions (`SpotUnavailableException`) over generic ones.
- **Inject a `Clock`**; never read system time inline (testability).
- Enums with behaviour/ordering (`VehicleSize.canFit(SpotSize)`) instead of if-else on type.

## Expected interview questions

1. **Q:** Why an interface here and not an abstract class? **A:** Nothing to share but the contract; abstract class only when subclasses share partial implementation. Composition stays open.
2. **Q:** Why is this field an enum and not a String? **A:** Illegal states become unrepresentable, transitions can live on the enum, and the compiler catches typos.
3. **Q:** Your model classes have no logic — isn't that anemic? **A:** Behaviour that belongs to one entity's invariants lives on the entity (e.g., `Seat.hold()`); cross-entity orchestration lives in services. Point at one example of each.
4. **Q:** Why `BigDecimal`/minor units for money? **A:** Binary floating point can't represent 0.1; rounding errors compound. Integer paise + explicit rounding rule is deterministic.
