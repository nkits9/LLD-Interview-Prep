# Pizza / Coffee Ordering

> **One-liner:** Builder + Decorator composing: the Builder assembles the base (required size, defaulted crust), decorators stack toppings — and the senior move is ONE data-configured `Topping` class instead of a class per topping.

## Scope & clarifying questions

**In scope:** sized/crusted base via Builder; toppings as decorators (stackable, repeatable); order with an itemized bill in paise; validation at construction. **Out:** menu/catalog, inventory, half-and-half pizzas (a Composite of two halves — name it), combos/discounts (a pricing Strategy over the order).

Ask: can a topping repeat? Do toppings price differently by pizza size *(then price becomes `f(size)` — a lambda per topping)*? Fixed menu or free composition?

## Approach vs alternatives

Chosen — **`BasePizza` (Builder, required-arg-in-constructor) + one `Topping` decorator configured by (name, price)**. Toppings differ only in data, so `CheeseTopping`/`OliveTopping`/`PaneerTopping` classes are the pattern-stuffing version of Decorator — collapsing them into one configured class is the same judgement as "levels differing only by limit = one class" in Chain of Responsibility. Alternatives: **subclass-per-combination** (`LargeCheeseBurstPaneerPizza`) — the combinatorial explosion Decorator exists to kill; **a `List<Topping>` field on Pizza** — honestly simpler(!) and worth saying: Decorator earns its keep when toppings carry *behaviour* (nutrition rules, cooking steps), not just a price sum.

## Key code

```java
// builder: required size is a constructor arg (can't compile an unsized pizza)
BasePizza.ofSize(LARGE).crust(CHEESE_BURST).build()

// decorator: is-a Pizza, has-a Pizza — stack freely, repeat freely
new Topping(new Topping(base, "paneer", 6_000), "olives", 3_000)
long costPaise() { return inner.costPaise() + pricePaise; }
```

## Must-cover edge cases

- [x] Required field (size) enforced at compile time via the builder's constructor
- [x] Same topping stacked twice — free with decorators, awkward with subclasses
- [x] Blank name / negative price rejected at construction
- [x] Bill sums in paise; display formatting is the only place division happens

## Interview follow-ups

1. **Q: Why not a class per topping?** **A:** They'd differ only in two constructor arguments. One configured class keeps the Decorator structure (composition, stacking) and deletes the ceremony — knowing when NOT to multiply classes is the signal.
2. **Q: When would `List<Topping>` inside Pizza beat Decorator?** **A:** When toppings are pure data (name+price) and you need to list/remove them — which decorators make hard (unwrapping). Decorator wins when each layer adds *behaviour*. Give the honest answer, then justify the pattern for the interview's sake.
3. **Q: Topping price depends on pizza size?** **A:** Price becomes `Function<Size, Long>` on the topping, and the decorator needs the base's size — expose it via the component interface. One-line seam, worth saying before being asked.

Run [`Demo.java`](Demo.java) — built bases, stacked (and repeated) toppings, an itemized bill, and two construction-time rejections.
