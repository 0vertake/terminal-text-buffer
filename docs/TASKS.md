# Implementation Tasks — Terminal Buffer

Follow this order exactly. Commit after each numbered step.
Never combine a new feature and a refactor in the same commit.

---

## Phase 1 — Project Scaffold

### Step 1 · Project setup
- [ ] Initialize Gradle Kotlin DSL project
- [ ] Write `build.gradle.kts` with JUnit 5 dependency (see `ARCHITECTURE.md`)
- [ ] Create package structure: `terminal/model/`, `terminal/`
- [ ] Verify `./gradlew build` compiles an empty project

**Commit**: `Initial project scaffold with Gradle and JUnit 5`

---

## Phase 2 — Data Model

### Step 2 · `TerminalColor`
- [ ] Implement `sealed class TerminalColor` with `Default` and `Standard(index)`
- [ ] Add `init { require(...) }` validation on `Standard`
- [ ] Write unit tests: valid indices, boundary indices (0, 15), invalid (−1, 16)

**Commit**: `Add TerminalColor sealed class with validation`

### Step 3 · `TextStyle` and `TextAttributes`
- [ ] Implement `data class TextStyle(bold, italic, underline)` with defaults false
- [ ] Implement `data class TextAttributes(foreground, background, style)` with defaults
- [ ] Write unit tests: default construction, custom construction, equality

**Commit**: `Add TextStyle and TextAttributes data classes`

### Step 4 · `Cell`
- [ ] Implement `data class Cell(char, isEmpty, attributes, charWidth=1)`
- [ ] Add `companion object { fun blank(): Cell }` factory
- [ ] Write unit tests: blank cell properties, custom cell, equality

**Commit**: `Add Cell data class with blank() factory`

### Step 5 · `BufferPosition`
- [ ] Implement `sealed class BufferPosition` with `Screen(row, col)` and `Scrollback(row, col)`

**Commit**: `Add BufferPosition sealed class`

---

## Phase 3 — TerminalLine

### Step 6 · `TerminalLine` — basic structure
- [ ] Implement `TerminalLine(width: Int)` backed by `Array<Cell>`
- [ ] Implement `getCell(col)` and `setCell(col, cell)` with bounds checks
- [ ] Implement `fillEmpty()` — reset all cells to `Cell.blank()`
- [ ] Implement `copy(): TerminalLine` — deep copy

**Commit**: `Add TerminalLine with cell access and copy`

### Step 7 · `TerminalLine` — content operations
- [ ] Implement `fill(char, attributes)` — fill all cells with given char/attrs
- [ ] Implement `asString()` — trim trailing empty cells, return content

**Commit**: `Add TerminalLine fill and asString operations`

### Step 8 · `TerminalLine` tests
- [ ] Test `getCell` / `setCell` at boundaries and out of bounds
- [ ] Test `fill` — all cells updated, attributes applied
- [ ] Test `fillEmpty` — cells reset to blank
- [ ] Test `asString` — trailing blank trim, explicit space retention, empty line
- [ ] Test `copy` — mutations to original do not affect copy

**Commit**: `Add comprehensive TerminalLine tests`

---

## Phase 4 — TerminalBuffer Core

### Step 9 · Constructor and initial state
- [ ] Implement `TerminalBuffer(width, height, maxScrollbackSize)`
- [ ] Validate arguments: `width >= 1`, `height >= 1`, `maxScrollbackSize >= 0`
- [ ] Initialize `screen` as `ArrayDeque` of `height` blank `TerminalLine`s
- [ ] Initialize `scrollback` as empty `ArrayDeque`
- [ ] Cursor at `(0, 0)`, attributes at `TextAttributes()`

**Commit**: `Add TerminalBuffer constructor with validation and initial state`

### Step 10 · Attributes
- [ ] Implement `setAttributes(attributes: TextAttributes)`
- [ ] Test: set attributes, verify subsequent writes use them

**Commit**: `Add setAttributes to TerminalBuffer`

### Step 11 · Cursor — get/set/move
- [ ] Implement `getCursorCol()`, `getCursorRow()`
- [ ] Implement `setCursor(col, row)` with clamping
- [ ] Implement `moveCursorUp/Down/Left/Right(n)` with clamping
- [ ] Test all cursor operations including boundary and overshoot cases

**Commit**: `Add cursor get/set/move with bounds clamping`

### Step 12 · Content access — screen only
- [ ] Implement `getChar(position: BufferPosition): Char`
- [ ] Implement `getAttributes(position: BufferPosition): TextAttributes`
- [ ] Implement `getLine(row, fromScrollback): String`
- [ ] Implement `getScreenContent(): String`
- [ ] Test all accessors on freshly initialized buffer (all blank)

**Commit**: `Add content access methods for screen`

### Step 13 · `writeText` — no scroll
- [ ] Implement `writeText(text)` for the case where text fits within current screen
- [ ] Handle wrapping within the screen (no scroll yet)
- [ ] Test: write in middle of screen, write that wraps, write empty string
- [ ] Test: cursor position after write

**Commit**: `Implement writeText without scroll`

### Step 14 · `writeText` — with scroll
- [ ] Add scroll-on-overflow: push top screen line to scrollback, add blank at bottom
- [ ] Enforce `maxScrollbackSize` — evict oldest when at capacity
- [ ] Test: write that causes single scroll, write that causes multiple scrolls
- [ ] Test: scroll at max scrollback capacity (oldest evicted)
- [ ] Test: cursor remains at `height - 1` after scroll

**Commit**: `Add scrollback and scroll-on-overflow to writeText`

### Step 15 · `insertText`
- [ ] Implement `insertText(text)` — push existing content right, drop overflow
- [ ] Cursor advances past inserted text (clamped)
- [ ] Test: insert at start, middle, end of line; insert longer than remaining space

**Commit**: `Implement insertText with push-right behavior`

### Step 16 · `fillLine`
- [ ] Implement `fillLine(row, char)` using current attributes
- [ ] Implement `fillLineEmpty(row)`
- [ ] Validate `row` is in `[0, height-1]`
- [ ] Test: fill row 0, fill last row, fill with custom attributes, invalid row throws

**Commit**: `Implement fillLine and fillLineEmpty`

### Step 17 · `insertEmptyLineAtBottom`
- [ ] Implement: push current top screen line to scrollback, add blank at bottom
- [ ] Handle scrollback max size eviction
- [ ] Test: scrollback grows, screen stays same height, overflow evicts oldest

**Commit**: `Implement insertEmptyLineAtBottom`

### Step 18 · `clearScreen` and `clearAll`
- [ ] Implement `clearScreen()` — blank all screen lines, keep scrollback, keep cursor
- [ ] Implement `clearAll()` — blank screen and clear scrollback
- [ ] Test: content gone, cursor unchanged, scrollback state correct for each

**Commit**: `Implement clearScreen and clearAll`

### Step 19 · Scrollback content access
- [ ] Extend `getChar`, `getAttributes`, `getLine` to support `BufferPosition.Scrollback`
- [ ] Implement `getFullContent()` — scrollback lines + screen lines, joined by `\n`
- [ ] Test: write enough to populate scrollback, verify `getFullContent()` order

**Commit**: `Add scrollback content access and getFullContent`

---

## Phase 5 — Integration Tests

### Step 20 · Scenario tests
- [ ] Simulate writing a paragraph of text longer than screen height
  — verify scrollback contents and screen content
- [ ] Simulate cursor navigation + targeted writes + verify content
- [ ] Simulate fill → clear → write cycle
- [ ] Simulate interleaved `writeText` and `insertEmptyLineAtBottom`

**Commit**: `Add integration scenario tests`

---

## Phase 6 — Solution Notes

### Step 21 · `SOLUTION_NOTES.md`
Write a markdown file at the project root covering:
- Design decisions and why (reference `ARCHITECTURE.md` choices you kept or changed)
- Trade-offs made
- Known limitations
- What you would improve with more time
- Thread safety disclaimer
- Bonus features: implemented or not, and why

**Commit**: `Add SOLUTION_NOTES.md`

---

## Phase 7 — Bonus Features (only after all above tests pass)

### Step 22 · Wide characters
- [ ] Add `isWide(char): Boolean` helper — check against Unicode wide ranges
- [ ] Modify `writeText` to detect wide chars and write placeholder
- [ ] Modify cursor advance to step by 2 for wide chars
- [ ] Modify `asString()` to skip placeholder cells (`'\u0000'`)
- [ ] Add `WideCharTest` with: write CJK, check cell content, check cursor, check string

**Commit**: `Add wide character support (bonus)`

### Step 23 · Resize
- [ ] Implement `resize(newWidth, newHeight)` using truncate/pad strategy
- [ ] Clamp cursor after resize
- [ ] Add `ResizeTest`: grow, shrink, grow+shrink, cursor clamping

**Commit**: `Add resize support (bonus)`

---

## Final Verification Checklist

Before the final commit:
- [ ] `./gradlew clean build` passes
- [ ] `./gradlew test` — all tests green, check test count is substantial
- [ ] No `TODO`, `FIXME`, or dead code
- [ ] `SOLUTION_NOTES.md` present and complete
- [ ] Git log looks clean and incremental (`git log --oneline`)
