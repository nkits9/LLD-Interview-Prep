# Snake & Ladder

> **One-liner:** The separation drill: the loop owns turns, the Board owns geography, the Dice owns chance (injected + seeded), the OvershootPolicy owns the house rule — swap any one without touching the others.

## Scope & clarifying questions

**In scope:** N players rotating; seeded dice; snakes/ladders as one validated jump map (no chains, no jump on square 1 or the last square); exact-finish vs bounce-back overshoot; win detection; game-over enforcement. **Out:** "6 grants another turn" / three-sixes-forfeit (turn-policy strategies — name them), multiple dice, kill-and-restart variants.

Ask: exact roll to finish, bounce, or overshoot-wins? Extra turn on a 6? Can two players share a square? Can a snake's tail sit on a ladder's base *(chain — decide: forbid or follow)*?

## Approach vs alternatives

Chosen — **one `Map<Integer,Integer>` for both snakes and ladders** (direction is derivable), validated at construction so illegal boards are unrepresentable — including **jump chains**, which is the classic missed edge case: forbid them (as here) or resolve in a loop with cycle detection; pick one *on purpose*. Dice wraps an injected `Random(seed)` — same testability rule as Clock. Overshoot is an enum policy, not an `if` in the loop. Alternatives: separate Snake and Ladder classes — ceremony for data that differs only in direction; resolving chains iteratively — fine, but say how you prevent 5→10→5 cycles.

## Key code

```java
int target = overshoot.apply(from + roll, board.size()); // -1 = needs exact, stays
int landed = board.resolve(target);                      // one map answers both jump types

// construction-time: a jump may not land on another jump (chains unrepresentable)
if (jumps.containsKey(to)) throw new IllegalArgumentException("jump chain at " + to);
```

## Must-cover edge cases

- [x] Chained jumps rejected at construction
- [x] Exact-finish rule: overshoot wastes the turn (STAY) / bounce-back available (BOUNCE)
- [x] Snake and ladder hits visible in the turn trace
- [x] Winner ends the game; further turns rejected explicitly
- [x] Seeded dice → fully reproducible game

## Interview follow-ups

1. **Q: Snake tail on a ladder base — what happens?** **A:** That's a jump chain; decide explicitly: forbid at construction (here — invalid board) or resolve in a loop with a visited-set against cycles. The failing answer is not having decided.
2. **Q: Where does "roll a 6, go again" live?** **A:** A turn policy in the loop's rotation step (don't re-enqueue the player), not in Board or Dice — the whole point of separating loop from rules.
3. **Q: Why inject Random?** **A:** Same as Clock: a seeded game is replayable in tests; `new Random()` inline makes every bug unreproducible.

Run [`Demo.java`](Demo.java) — a rejected chained board, a full seeded game with snake/ladder hits and the exact-finish rule, and the game-over rejection.
