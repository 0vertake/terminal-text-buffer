# Agent Instructions — Terminal Buffer (JetBrains Internship Task #2)

## Overview

You are implementing a **terminal text buffer** in Kotlin — the core data structure
that terminal emulators use to store and manipulate displayed text.
This is a JetBrains internship coding task. The output must be production-quality:
clean architecture, comprehensive tests, and a clear git history.

## Project Layout

```
src/
  main/kotlin/terminal/
    model/          ← pure data types (Cell, TextAttributes, TerminalColor, TextStyle)
    TerminalLine.kt ← a single row in the buffer
    TerminalBuffer.kt ← the main class
  test/kotlin/terminal/
    TerminalBufferTest.kt
    TerminalLineTest.kt
    CursorTest.kt
    ScrollbackTest.kt
    AttributesTest.kt
    WideCharTest.kt   (bonus)
    ResizeTest.kt     (bonus)
docs/
  SPEC.md           ← full specification
  ARCHITECTURE.md   ← class design and decisions
  TASKS.md          ← ordered implementation checklist
AGENTS.md           ← this file
```

## Constraints — Read Before Writing Any Code

- **Language**: Kotlin only
- **Build tool**: Gradle (Kotlin DSL — `build.gradle.kts`)
- **No external libraries** except for testing
- **Test framework**: JUnit 5 (Jupiter) — already standard with Gradle
- **No state mutation from outside** — all buffer state must go through
  `TerminalBuffer` public API
- The project must compile with `./gradlew build`
- All tests must pass with `./gradlew test`

## Detailed Spec

See [`docs/SPEC.md`](docs/SPEC.md) for the full operation specification.

## Architecture Guide

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the class design,
data model rationale, and key decisions to make.

## Implementation Order

See [`docs/TASKS.md`](docs/TASKS.md) for a step-by-step checklist.
**Follow this order strictly** — each step builds on the previous one,
and commits should map to these steps.

## Git Discipline

This is evaluated. Every commit must:
- Be a single logical unit (one feature or one refactor — never both)
- Have a clear, descriptive message in imperative mood:
  - ✅ `Add Cell data class with attributes`
  - ✅ `Implement cursor movement with bounds clamping`
  - ✅ `Add scrollback eviction on screen overflow`
  - ❌ `stuff` / `fix` / `wip`
- Not break the build (each commit should compile and tests should pass at that point)

Suggested commit sequence is in `docs/TASKS.md`.

## Quality Bar

- Every public method must have a corresponding test
- Tests must cover edge cases (see `docs/SPEC.md` for a list per operation)
- Use descriptive test names: `writeText_wrapsToNextLineWhenExceedingWidth()`
- Prefer `@Nested` JUnit 5 classes to group related tests
- No `TODO` or `fixme` left in submitted code — either implement or document
  in `SOLUTION_NOTES.md`

## Bonus Features (implement after core is complete)

1. **Wide characters** — CJK / emoji occupy 2 columns. See `docs/SPEC.md#wide-characters`.
2. **Resize** — change screen dimensions. See `docs/SPEC.md#resize`.

Only attempt bonus after all core tests pass.

## Final Deliverable Checklist

Before considering the task done:
- [ ] `./gradlew build` passes with zero warnings if possible
- [ ] `./gradlew test` — all tests green
- [ ] `SOLUTION_NOTES.md` written (explanation of decisions and trade-offs)
- [ ] Git log shows incremental, well-named commits
- [ ] No dead code, no commented-out blocks
