# Singleton

> **Solves:** Exactly one shared instance with global access. **Know the thread-safe forms and the criticism — and don't use it unless asked.**

## When to use / when NOT

- **Use when:** the interviewer explicitly wants one shared registry/manager, or a resource is genuinely single (process-wide config, connection pool handle).
- **Don't use when:** almost always otherwise — "manager" is not a reason. Red flag: `XyzManager.getInstance()` sprinkled through the code.
- **Say aloud:** "I'd normally create one instance at composition root and inject it — easier to test and no hidden coupling. If you want a hard singleton, I'll use the holder idiom."

## The four thread-safe forms (write the first two from memory)

| Form | Lazy? | Cost | Notes |
|------|:-----:|------|-------|
| **Holder idiom** (this repo's default) | ✅ | zero locking | JVM class-init guarantees exactly-once; the answer that ends the discussion |
| **Double-checked locking** | ✅ | 1 volatile read | The grilling favourite — `volatile` is mandatory (see below) |
| Eager `static final` | ❌ | zero | Fine when construction is cheap and always needed |
| `enum` singleton | ❌ | zero | Also immune to reflection and serialization attacks — say this |

(`synchronized` on the whole `getInstance()` also works — correct but serializes every call.)

## Key code — the 20% that matters

```java
public final class AppConfig {
    private AppConfig() {}                       // no outside instantiation

    private static final class Holder {          // loaded only on first getInstance()
        private static final AppConfig INSTANCE = new AppConfig();
    }
    public static AppConfig getInstance() { return Holder.INSTANCE; }
}
```

```java
private static volatile DoubleCheckedConfig instance;      // volatile: mandatory

public static DoubleCheckedConfig getInstance() {
    DoubleCheckedConfig local = instance;                  // fast path: one volatile read
    if (local == null) {
        synchronized (DoubleCheckedConfig.class) {
            local = instance;
            if (local == null) instance = local = new DoubleCheckedConfig();
        }
    }
    return local;
}
```

Run [`Demo.java`](Demo.java) — 100 threads race both forms; each yields exactly one instance.

## Where it appears in the 12 problems

- **Logging Service** — "thread-safe global access **without a hard Singleton**" is the actual requirement: one logger instance created at startup and injected. Use the pattern's ideas, dodge its coupling.

## Expected interview questions

1. **Q: Why does double-checked locking need `volatile`?**
   **A:** `new` is three steps — allocate, construct, assign reference — and without volatile they can reorder so another thread sees a **non-null but partially constructed** object at the first (unsynchronized) check. Volatile's happens-before forbids that publish-before-construct reordering.
2. **Q: Why does the holder idiom need no locks at all?**
   **A:** The JLS guarantees a class is initialized exactly once, with initialization safely published. `Holder` isn't loaded until `getInstance()` first touches it — the JVM does the lazy-and-once part for free.
3. **Q: What's wrong with Singleton?**
   **A:** It's global mutable state: hidden dependencies (nothing in a signature says it's used), test pollution across cases, impossible to substitute/mock cleanly, and it couples everyone to one concrete class. Prefer one instance created at the composition root and **injected**.
4. **Q: Why enum for singletons?**
   **A:** JVM guarantees one instance even against reflection (`Constructor.setAccessible`) and serialization (readResolve problems) — the two classic breaks of hand-rolled singletons.
5. **Q: Singleton vs static utility class?**
   **A:** Statics can't implement an interface, be injected, or be swapped for tests. If the thing has state or needs polymorphism, it's an instance; if it's pure stateless functions, statics are fine.

## Write-from-memory checklist (target: 3 minutes)

- [ ] `final` class, private constructor
- [ ] Holder idiom: private static nested class holding the `INSTANCE`
- [ ] DCL variant: `volatile` field, check → lock → re-check
- [ ] Say the criticism + "inject instead" unprompted
- [ ] Demo: N threads race, identity set size == 1
