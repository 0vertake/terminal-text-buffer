# Architecture — Terminal Buffer

## Class Diagram

```
terminal/
├── model/
│   ├── TerminalColor.kt      sealed class: Default | Standard(index)
│   ├── TextStyle.kt          data class: bold, italic, underline
│   ├── TextAttributes.kt     data class: fg, bg, style
│   ├── Cell.kt               data class: char, isEmpty, attributes, charWidth
│   └── BufferPosition.kt     sealed class: Screen(row,col) | Scrollback(row,col)
├── TerminalLine.kt            wraps Array<Cell>, row-level operations
└── TerminalBuffer.kt          main class — screen + scrollback + cursor + attributes
```

---

## Model Layer — Key Decisions

### `TerminalColor` as sealed class (not enum)

An enum would work for the 16 standard colors, but a sealed class
allows `Default` to carry no data while `Standard` carries an index.
This cleanly models the distinction between "no color set" and
"color index 0" (which are different things).

```kotlin
sealed class TerminalColor {
    object Default : TerminalColor()
    data class Standard(val index: Int) : TerminalColor() {
        init { require(index in 0..15) { "Color index must be 0..15, got $index" } }
    }
}
```

### `Cell.isEmpty` vs using a sentinel char

Using `isEmpty: Boolean` is cleaner than using a sentinel character like
`'\u0000'` to mean "empty", because:
- A space `' '` can be explicitly written with attributes (e.g., highlighted background)
- An empty cell has no attributes — they are semantically different

Exception: wide char placeholders use `char = '\u0000'` and `isEmpty = false`
to indicate "second half of a wide character".

### `Cell.charWidth`

Always `1` for core implementation. Set to `2` for wide characters (bonus).
When the agent encounters a wide char, it writes it at column `c` with
`charWidth=2`, and fills `c+1` with a placeholder cell.

---

## `TerminalLine` Internals

```kotlin
class TerminalLine(val width: Int) {
    private val cells: Array<Cell> = Array(width) { Cell.blank() }

    fun getCell(col: Int): Cell
    fun setCell(col: Int, cell: Cell)
    fun fill(char: Char, attributes: TextAttributes)
    fun fillEmpty()
    fun asString(): String  // trims trailing empty cells
    fun copy(): TerminalLine  // deep copy for scrollback
}
```

Use `Array<Cell>` (not `List`) for O(1) random access. Lines are always
fixed-width — if width changes (resize bonus), create a new `TerminalLine`.

`copy()` is needed when pushing lines to scrollback — scrollback lines
must be immutable snapshots of screen state.

---

## `TerminalBuffer` Internals

```kotlin
class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollbackSize: Int = 1000
) {
    // Screen: mutable, fixed size (height lines)
    private val screen: ArrayDeque<TerminalLine>

    // Scrollback: oldest at index 0, newest at last index
    private val scrollback: ArrayDeque<TerminalLine>

    var cursorCol: Int = 0
        private set
    var cursorRow: Int = 0
        private set

    var currentAttributes: TextAttributes = TextAttributes()
        private set
    ...
}
```

### Why `ArrayDeque` for screen?

The screen behaves like a fixed-size sliding window: when new content
pushes a line off the top, we `removeFirst()` and `addLast()`. `ArrayDeque`
makes both ends O(1). The screen always contains exactly `height` lines.

### Why `ArrayDeque` for scrollback?

Same reason — we evict from the front (oldest) when at capacity.
Append to back = newest scrollback line. Index 0 = oldest.

### Scroll-on-write Logic

When `writeText` advances the cursor past `height - 1`:
```
1. Take screen.removeFirst() → push to scrollback.addLast()
2. If scrollback.size > maxScrollbackSize → scrollback.removeFirst()
3. screen.addLast(TerminalLine(width))  // new blank line at bottom
4. cursorRow stays at height - 1
```

### `BufferPosition` sealed class

```kotlin
sealed class BufferPosition {
    data class Screen(val row: Int, val col: Int) : BufferPosition()
    data class Scrollback(val row: Int, val col: Int) : BufferPosition()
}
```

This gives callers a type-safe way to address both regions without
negative indices or magic numbers. Internal resolvers map to the
correct `ArrayDeque`.

---

## Coordinate Conventions

| What | Convention |
|------|------------|
| Screen rows | 0-indexed, 0 = top |
| Screen cols | 0-indexed, 0 = left |
| Scrollback rows | 0-indexed, 0 = oldest (farthest from screen) |
| Cursor | Always in screen coordinates |
| Bounds violation on setters | Clamp (cursor) or throw (getChar with invalid pos) |

---

## Thread Safety

Not required for this task. `TerminalBuffer` is **not** thread-safe.
Document this in `SOLUTION_NOTES.md`.

---

## Immutability Strategy

- `TextAttributes`, `TextStyle`, `Cell`, `TerminalColor` — immutable data classes
- `TerminalLine` — mutable (cells are set individually for performance)
- `TerminalBuffer` — mutable (it is a stateful buffer by definition)
- When pushing a line to scrollback: always push `line.copy()` to avoid
  mutation of scrollback entries if the screen line is later modified

---

## `getScreenContent()` and `getFullContent()` — Trailing Whitespace

`getLine()` should trim trailing empty cells when converting to string.
This makes test assertions much more readable:
```kotlin
// Without trim: "hello                   "
// With trim:    "hello"
```

Explicitly written spaces with non-default attributes should **not** be trimmed
(since `isEmpty=false` for them).

---

## Resize Strategy (Bonus)

Recommended approach: **Truncate/Pad** (simpler, sufficient for this task).

1. For each line in screen: if line is wider than `newWidth`, truncate;
   if narrower, pad with blank cells.
2. If `newHeight > height`: add blank lines at bottom.
3. If `newHeight < height`: remove lines from the bottom, push them to scrollback.
4. Clamp cursor to new bounds.

Document this choice in `SOLUTION_NOTES.md` and mention reflow as the
more realistic alternative.

---

## Test Organization (JUnit 5)

```kotlin
class TerminalBufferTest {

    @Nested inner class CursorTests { ... }
    @Nested inner class WriteTextTests { ... }
    @Nested inner class InsertTextTests { ... }
    @Nested inner class FillLineTests { ... }
    @Nested inner class ScrollbackTests { ... }
    @Nested inner class ContentAccessTests { ... }
    @Nested inner class ClearTests { ... }
}

class WideCharTest { ... }   // bonus
class ResizeTest { ... }     // bonus
```

Use `@BeforeEach` to set up a standard buffer (e.g., 10×5 with scrollback 20)
for most tests. Use custom-sized buffers in tests that specifically need them.

---

## `build.gradle.kts` Skeleton

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.milos.terminal"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
```
