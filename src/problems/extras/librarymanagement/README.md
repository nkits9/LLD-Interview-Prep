# Library Management

> **One-liner:** The CRUD/Repository problem whose two real rules are: fines computed from an injected clock, and a returned copy honors the reservation queue BEFORE hitting the open shelf. Book (metadata) vs BookCopy (the lendable thing) is the modelling split.

## Scope & clarifying questions

**In scope:** books with multiple physical copies; checkout with a per-member limit; due dates + per-day fines (paise, injected clock); FIFO reservations per ISBN, auto-issued on return; explicit failures for no-copy, over-limit, and bogus returns. **Out:** membership tiers, renewals (a due-date extension with a re-reservation check), lost-book workflows, search/catalog UX.

Ask: what's lent — a title or a physical copy? *(copy — the split matters)* Fine policy and cap? Does a reservation hold the copy or just queue for it? Borrow limit per member?

## Approach vs alternatives

Chosen — **Book/BookCopy split** (metadata vs lendable unit — one ISBN, N copies, each independently on loan) with `Map<Id, Entity>` repositories, and **return-time reservation fulfillment**: `returnCopy` polls the ISBN's queue and re-lends immediately, so a reserved copy is never visible as available (no check-then-act window for shelf snipers). Alternatives: **available-count on Book** — loses copy identity (which physical copy is overdue?); **reservation as a hold with TTL** (locker/booking style) — the upgrade when members must collect within N days: the auto-issue becomes a hold + expiry sweep, both patterns already in this repo.

## Key code

```java
// fines: injected clock, integer paise, explicit day math
if (today.isAfter(loan.dueDate()))
    finePaise = ChronoUnit.DAYS.between(loan.dueDate(), today) * FINE_PER_DAY_PAISE;

// reservations beat the open shelf — fulfilled inside the return, atomically
Deque<String> queue = reservations.get(copy.isbn());
if (queue != null && !queue.isEmpty()) lend(copy, queue.pollFirst());
```

## Must-cover edge cases

- [x] All copies out → `NoCopyAvailableException`, reserve queues FIFO
- [x] Returned copy auto-issued to the first reservation (shelf shows 0 available)
- [x] Late return → deterministic fine (6 days × ₹10 in the demo)
- [x] Borrow limit blocks a 4th book even with copies on the shelf
- [x] Returning a copy that isn't on loan → explicit error

## Interview follow-ups

1. **Q: Why Book AND BookCopy?** **A:** You lend atoms, not metadata: two members hold "the same book" as different copies with different due dates. Modelling only Book forces an availability counter and loses per-copy state (condition, location, which one is overdue) — the same fungibility question as hotel-vs-train, answered the other way.
2. **Q: Member returns a reserved book — can a walk-in grab it first?** **A:** No: fulfillment happens inside the synchronized return — the copy transitions loan→loan without ever being shelf-visible. Exposing it as available and letting the reserver race is the check-then-act bug.
3. **Q: Fines pile up forever?** **A:** Cap at the book's replacement cost, then flip the loan to a lost-book workflow — a fine policy Strategy; the clock injection is what makes any of it testable.

Run [`Demo.java`](Demo.java) — checkouts, a queued reservation honored on a late return with the fine computed, the borrow limit, and a bogus return rejected.
