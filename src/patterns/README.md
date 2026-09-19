# Design Patterns

Implementations of the 11 patterns worth writing from memory. Cheatsheet: [docs/patterns-cheatsheet.md](../../docs/patterns-cheatsheet.md).

## Layout

```
patterns/
├── creational/    factory/  builder/  singleton/
├── behavioral/    strategy/  state/  observer/  command/  chainofresponsibility/  memento/
└── structural/    decorator/  composite/
```

## Conventions

- One lowercase package per pattern: `patterns.behavioral.strategy`.
- Each folder is self-contained: `README.md` (copied from [templates/PATTERN_DOC_TEMPLATE.md](../../templates/PATTERN_DOC_TEMPLATE.md)) + minimal classes + runnable `Demo.java`.
- Keep each implementation small enough to write in an interview (~3 minutes) — the goal is memory, not completeness.
- Factory and Abstract Factory live together in `creational/factory/`.
- Template Method, Facade, Adapter, Visitor are recognize-only — documented in the cheatsheet, no code.
