# Problems

The core 12, solved in roadmap order — each introduces one or two new concepts and reuses earlier ones. Index with status: [root README](../../README.md).

## Layout & conventions

- Numbered packages preserve solve order: `problems.p01_connectfour` … `problems.p12_texteditor`. Post-core problems (order matching, LRU cache, vending machine, …) go under `problems/extras/<name>/`.
- Each folder is self-contained:
  - `README.md` — revision doc, copied from [templates/PROBLEM_DOC_TEMPLATE.md](../../templates/PROBLEM_DOC_TEMPLATE.md)
  - code, with subpackages only where they earn their keep: `model/`, `service/`, `strategy/`, `exception/`
  - `Demo.java` — thin driver, **no business logic**, exercising one happy path *and* one failure path (lot full, payment failure, illegal transition)
- Code in interview order: enums → models → interfaces → strategy impls → services → driver.
- Apply the [done-checklist](../../docs/interview-playbook.md#done-checklist--before-calling-any-problem-complete) before marking a problem ✅ in the root README.
