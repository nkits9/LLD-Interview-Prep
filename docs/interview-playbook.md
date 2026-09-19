# Interview Playbook

How to run the 60 minutes, how to talk, and what fails SDE3 candidates. This applies to **every** problem — read it first, re-read it last.

## The 60-minute rhythm

| Time | Do | What's being judged |
|------|----|---------------------|
| 0–7 min | Ask 5–8 clarifying questions. Write down 3–5 in-scope features. Explicitly say what's out of scope. | Can you scope, or do you just start coding? |
| 7–15 min | Nouns → classes, verbs → methods. Say relationships aloud ("Floor has many Spots, Spot holds zero or one Vehicle"). Sketch boxes with has-a / is-a arrows. Don't UML-perfect it. | Entity modelling |
| 15–20 min | Identify where change will come (new type, new rule, new channel). Only here do patterns go in: "Pricing will vary, so PricingStrategy interface." | Extensibility judgement |
| 20–50 min | Code in this order: **enums → models/entities → interfaces → strategy impls → services → thin driver**. Get one working vertical slice before covering every feature. | Working, clean code |
| 50–60 min | Walk one flow end-to-end. Discuss concurrency ("two users at once → where's the lock?"). Handle the "now add X" extension. | Depth and adaptability |

Practice with a timer until this rhythm is automatic.

## Communication rules

- Narrate decisions as trade-offs: "Interface here because X will vary; if it won't, an enum is simpler."
- On pushback, don't defend reflexively: "That's fair — the alternative is Y, and I'd pick it when…"
- Code in real Java with real syntax; pseudocode reads as weakness at this level.
- Never say "in production I'd do it differently" without saying how.
- Leave TODO comments for skipped items and say them aloud.
- For every pattern and every lock: one sentence on why it's there — **and why not elsewhere**.

## Mistakes that fail SDE3 candidates

- Jumping to code without scoping.
- Pattern stuffing — actively penalized at this level.
- Deep inheritance hierarchies.
- Not finishing a runnable flow — 80% coded beats 100% designed with nothing running.
- Hand-waving concurrency ("I'd use a lock") without saying where or how granular.
- Check-then-act on shared state (the classic race).
- Holding a lock across an external call or user think-time.
- Business logic in the driver/main.
- Freezing on the extension question.
- Only demonstrating the happy path.

## Extension questions — pre-think for every problem

Your design must absorb each with minimal edits. If it can't, redesign.

- Add a new type (vehicle/piece/split/channel) → one new class only.
- Change a rule (pricing, allocation, matching) → swap a strategy.
- Multi-tenant / multi-location → where does the extra key go?
- Persist it → which classes become repositories; which isolation level?
- Two concurrent requests → where's the lock, how small, what if it doesn't scale?
- Make it observable / auditable → Observer or event log hook.
- Make it distributed → what breaks (locks, clocks, idempotency)? Name the fix, don't code it.

## Done-checklist — before calling any problem complete

| ✓ | Check |
|---|-------|
| ☐ | Two clients hit the same resource simultaneously: where's the lock, and how small is it? |
| ☐ | Any operation touching multiple resources acquires locks in a global order or uses optimistic versions. |
| ☐ | No lock is held across I/O or user think-time; reserve-with-TTL used instead. |
| ☐ | Retried requests are idempotent. |
| ☐ | Every invalid input and illegal state transition raises a specific exception. |
| ☐ | Money is never a double; time is never read directly (inject a clock). |
| ☐ | Adding a new type or rule requires zero edits to existing classes. |
| ☐ | One runnable driver exercises a non-happy path (failure, timeout, rollback), not just success. |
| ☐ | One sentence explaining why each pattern is there — and why you didn't use one elsewhere. |
| ☐ | All extension questions above answered. |
