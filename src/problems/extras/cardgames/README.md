# Card Games (Blackjack)

> **One-liner:** Many small entities (Card/Rank/Suit/Deck/Hand) + a rule engine — the soft-ace valuation is the heart, house rules are Strategies (stand-on-17 vs hit-soft-17 is a REAL casino variant), and a tiny phase machine guards action ordering.

## Scope & clarifying questions

**In scope:** 52-card seeded deck; hand valuation with soft aces; player hit/stand; dealer plays by pluggable strategy; naturals, busts, push; settled-round enforcement. **Out (say it):** betting/payouts (3:2 vs 6:5 — a payout Strategy), split/double-down/insurance (each is a player-action subtype), multi-deck shoe + cut card, card counting countermeasures.

Ask: which dealer rule — S17 or H17? Payouts in scope? Split/double? One player or seats? *(seats = a list of hands, the loop doesn't change)*

## Approach vs alternatives

Chosen — **value on `Rank` (ace = 1), duality in `Hand`**: sum everything with aces as 1, then promote exactly one ace to 11 if it fits (`sum + 10 <= 21`). Alternatives: tracking each ace's chosen value as state — the classic over-modelling; there's never a reason to promote two aces (22), so the one-line rule is complete. Dealer behaviour as a **Strategy** (`standOnAll17s` / `hitSoft17`) because the variation is real and data-driven; a `Dealer` subclass hierarchy for one boolean of variation is the anti-pattern. Rounds carry a **phase enum** so `playerHit` after settlement throws — same illegal-transition discipline as every lifecycle in this repo.

## Key code

```java
// the entire soft-ace engine
int sum = cards.stream().mapToInt(c -> c.rank().value()).sum();  // aces as 1
return (hasAce && sum + 10 <= 21) ? sum + 10 : sum;              // promote ONE if it fits

// house rules as strategies — a one-line, testable difference
static DealerStrategy standOnAll17s() { return h -> h.bestValue() < 17; }
static DealerStrategy hitSoft17()     { return h -> h.bestValue() < 17
                                                 || (h.bestValue() == 17 && h.isSoft()); }
```

## Must-cover edge cases

- [x] A+6 = 17 soft; A+6+9 = 16 (ace demotes automatically)
- [x] Natural blackjack settles immediately (push if the dealer also has one)
- [x] Player bust loses before the dealer acts
- [x] Acting on a settled round → explicit exception; reading an unfinished outcome too
- [x] Seeded deck → identical replays; deck exhaustion is an explicit error

## Interview follow-ups

1. **Q: How do you value a hand with three aces?** **A:** Count all aces as 1, then promote at most one to 11 if the total stays ≤21 — promoting two adds 20 and always busts, so "each ace decides" is over-modelling. A+A+9 = 11+1+9 = 21, by one line.
2. **Q: Where do split and double-down go?** **A:** Player actions on the round's phase machine: split forks a Hand into two child hands played in sequence; double = one forced hit + stand with a doubled stake. The phase enum grows states; Hand and Deck don't change.
3. **Q: Poker instead — what transfers?** **A:** Card/Deck/Hand verbatim; the rule engine swaps to hand-ranking (a `Comparator<Hand>` built from evaluators — flush, straight, pairs). That's why entities and rules are separate layers.

Run [`Demo.java`](Demo.java) — soft-ace arithmetic, a full seeded round, the settled-round rejection, the S17/H17 strategy split on a soft 17, and three reproducible rounds.
