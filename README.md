# Terminal Text Buffer

A Kotlin library that simulates a terminal screen buffer with cursor movement, text attributes, line wrapping, and scrollback history.

## What It Does

- Maintains a fixed-size visible screen (`width` x `height`)
- Stores scrolled-off lines in scrollback history
- Supports cursor movement with bounds clamping
- Writes text with wrapping and automatic scrolling
- Supports single-line insert mode (`insertText`) with right-shift and overflow drop
- Stores per-cell text attributes (foreground, background, style)
- Supports basic wide-character rendering (2-cell width with placeholder)

## Project Structure

- `src/main/kotlin/terminal/` - Core implementation (`TerminalBuffer`, `TerminalLine`, model classes)
- `src/test/kotlin/terminal/` - Unit and integration tests
- `docs/SPEC.md` - Functional requirements
- `docs/ARCHITECTURE.md` - Architecture constraints and design notes
- `docs/TASKS.md` - Step/task breakdown
- `SOLUTION_NOTES.md` - Trade-offs, limitations, and implementation notes

## Prerequisites

- JDK 17+ (recommended for current Gradle/Kotlin setup)
- Windows PowerShell (commands below use PowerShell syntax)

## Build And Test

Run from the repository root:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

Run a focused test class:

```powershell
.\gradlew.bat test --tests "terminal.TerminalBufferTest"
```

Open the HTML test report:

```powershell
start .\build\reports\tests\test\index.html
```

## How Reviewers Should Validate

This repository is a library-style project (no application `run` task).
Validate using build and tests:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

## Notes

- Wide character handling is `Char`-based and does not fully support surrogate-pair emoji/grapheme clusters.
- `TerminalBuffer` is not thread-safe; synchronize externally if used across threads.

