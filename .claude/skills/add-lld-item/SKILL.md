---
name: add-lld-item
description: Add a new design pattern, LLD problem, or concept doc to this interview-prep repo following its structure, templates, and quality bar. Use whenever implementing a pattern ("add Adapter pattern"), solving a design question ("implement p02 Amazon Locker", "solve Splitwise"), or adding a cheatsheet/concept doc.
---

# Adding items to the LLD Interview Prep repo

This repo preps for SDE3 LLD interviews. Everything follows **80/20**: interview-sized code, revision-first docs, nothing overwhelming. Every item is self-contained (code + README revision doc + runnable `Demo.java`) and lands as one commit that flips its ☐→✅ in the root README tracker.

**The spec is [LLD.md](../../../LLD.md)** — every problem has a section there listing its patterns, concurrency focus, must-cover items, and extension. Read it first; you are not done until every must-cover is **in code**, not just discussed.

## Where things go

| Item | Location | Package |
|------|----------|---------|
| Pattern | `src/patterns/{creational,behavioral,structural}/<pattern>/` | `patterns.behavioral.strategy` |
| Core problem | `src/problems/pNN_<name>/` (numbered, roadmap order) | `problems.p03_parkinglot` |
| Extra problem | `src/problems/extras/<name>/` | `problems.extras.lrucache` |
| Concept doc | `docs/<name>.md` | — |

Plain Java, no build tool, no dependencies. `src` is the sources root. One public class per file. Flat package per item — subpackages (`model/`, `service/`, `strategy/`, `exception/`) only when a problem is big enough that they earn their keep.

## Workflow: new design pattern

1. **Pick a domain that feeds the core 12 problems** (pricing → Parking Lot, order lifecycle → Food Delivery, notifier → Logging). Never `Foo`/`Bar`.
2. **Interview-sized**: the core must be writable from memory in ~3 minutes. Interface → 2 concrete impls → context/client → `Demo.java`. The structure must match the canonical GoF / refactoring.guru shape — interviewers pattern-match against it; any deviation needs a stated reason in the README.
3. **Demo must show**: the swap/composition that justifies the pattern, AND one failure path (specific exception, retry exhausted, illegal state).
4. **README from [templates/PATTERN_DOC_TEMPLATE.md](../../../templates/PATTERN_DOC_TEMPLATE.md)** — all sections:
   - the one-line "solves", when to use / when NOT (pattern-stuffing red flag), and the **say-aloud sentence** ("X will vary, so...; if it wouldn't, Y is simpler")
   - small mermaid class diagram (3–5 boxes, the shape not the ceremony)
   - ~15-line key-code block, where it appears in the 12 problems, 5 interview Q&As **with answers and trade-offs**, 3-minute write-from-memory checklist
5. Include the honest alternative in the Q&A (switch instead of Strategy, enum table instead of State classes) — naming when NOT to use the pattern is the SDE3 signal.

## Workflow: new problem

1. **Read the problem's LLD.md section** — patterns, concurrency, must-covers, extension. That's the scope; don't invent more.
2. **Validate the approach BEFORE coding (mandatory).** One solution has multiple approaches; only the interview-consensus one is acceptable. Web-search how the standard references solve it — `ashishps1/awesome-low-level-design` (check `problems/<name>.md` and `solutions/java/src/<name>`), Hello Interview / AlgoMaster LLD write-ups, refactoring.guru for pattern shapes — and implement that consensus design. Where a known optimal data structure/algorithm exists, it IS the expected answer — use it and state its complexity in the README (examples: last-move-only win check for board games; HashMap + doubly-linked list for O(1) LRU; TreeMap/heaps with price-time priority for order books; min-cash-flow graph settlement for Splitwise; lazy refill + CAS for token buckets). If credible references diverge, pick the approach LLD.md implies and record the alternative and why in the README's "Approach vs alternatives" line and follow-up Q&A.
3. **Scope in the README first**: 3–5 in-scope features, explicit out-of-scope list, 5–8 clarifying questions you'd ask.
4. **Code in interview order**: enums → models/entities → interfaces → strategy impls → services → thin `Demo` driver. Get one vertical slice working before covering everything.
5. **Patterns only where the roadmap/change-vector justifies them**; record the one-sentence why in README §4, and record deliberate NON-uses ("no Factory for 2 players — pattern stuffing").
6. **Concurrency is code, not commentary** where the roadmap says so (lock per locker, CAS on assignment, bounded queue). Name shared state, lock granularity, and trade-off in README §5.
7. **Every must-cover from LLD.md is in code** with a specific exception or visible behavior.
8. **Demo**: happy path AND at least one failure path (full lot, payment failure, illegal transition). No business logic in the driver.
9. **README from [templates/PROBLEM_DOC_TEMPLATE.md](../../../templates/PROBLEM_DOC_TEMPLATE.md)** — all sections, mermaid entity diagram, the "Approach vs alternatives" line, only the tricky 2–3 code snippets, extension answers, 5 follow-up Q&As.
10. Apply the [done-checklist](../../../docs/interview-playbook.md#done-checklist--before-calling-any-problem-complete) before calling it complete.

## Workflow: new concept doc

`docs/<kebab-name>.md`, 1–2 pages max, tables over prose, distilled from LLD.md or the source material — never a dump. Link it from the root README revision path only if it belongs in the 2-hour pre-interview read.

## Hygiene rules (apply everywhere)

- Money in minor units (`long` paise) or `BigDecimal` — never `double`. Inject `Clock`; never read time inline.
- Specific exceptions (`SpotUnavailableException`), validate at construction, immutable value objects, `Map<Id, Entity>` repositories, enums over string statuses.
- Comments only where they state a constraint or interview-worthy "why" (e.g., "volatile: mandatory, see README") — no narration.
- Prefer `ConcurrentHashMap.compute` / atomics over check-then-act; validate before mutating so failed operations leave state untouched.

## Verify & ship (every item)

```bash
# from repo root — out/ is gitignored
cd src && javac -d ../out $(find <item-dir> -name '*.java') && java -cp ../out <package>.Demo
```

1. Demo output must show the happy path AND the failure path working. Fix until it does.
2. Flip the item's ☐→✅ row in the root [README.md](../../../README.md) tracker.
3. If a new pattern: check its link/row in [docs/patterns-cheatsheet.md](../../../docs/patterns-cheatsheet.md) still holds.
4. One commit per item: `Add <pattern> pattern (<domain>) with revision doc` / `Add pNN <Problem> (<key concept>) with revision doc`.
5. Push. Git identity and credentials are **repo-local** (personal account `nkits9`, `nkits9@gmail.com`) — never run `gh auth switch`, never touch global git config; if auth fails, check `gh auth status` lists `nkits9` in the keyring.
