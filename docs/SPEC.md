# Specification — Terminal Buffer

## Coordinate System

- Columns and rows are **0-indexed** internally.
- `(col=0, row=0)` is the **top-left** of the screen.
- `col` ranges from `0` to `width - 1`.
- `row` ranges from `0` to `height - 1` (screen only).
- Scrollback rows are indexed negatively or by a separate scrollback index
  (design decision — see `ARCHITECTURE.md`).

---

## Data Model

### `TerminalColor`

A sealed class with two variants:
- `Default` — the terminal's default color (no explicit color set)
- `Standard(index: Int)` — one of 16 ANSI colors, `index` in `0..15`

Validation: `Standard` must reject indices outside `0..15`.

### `TextStyle`

A data class with three boolean flags:
- `bold: Boolean`
- `italic: Boolean`
- `underline: Boolean`

Default: all `false`.

### `TextAttributes`

A data class combining:
- `foreground: TerminalColor` (default: `Default`)
- `background: TerminalColor` (default: `Default`)
- `style: TextStyle` (default: all false)

### `Cell`

A data class representing one grid position:
- `char: Char` — the character stored (use `' '` for visually empty cells,
  but distinguish from an explicitly written space using `isEmpty`)
- `isEmpty: Boolean` — `true` if the cell has never been written to
- `attributes: TextAttributes`
- `charWidth: Int` — `1` for normal characters, `2` for wide characters (bonus).
  A wide character occupies cells `[col]` and `[col+1]`; the second cell should
  be a **placeholder** `Cell` with `isEmpty=false` and a sentinel char (e.g., `'\u0000'`).

A **blank cell** is `Cell(char=' ', isEmpty=true, attributes=TextAttributes(), charWidth=1)`.

### `TerminalLine`

Represents one row. Internally an `Array<Cell>` of fixed `width`.

Operations:
- `getCell(col): Cell`
- `setCell(col, cell)`
- `fill(char, attributes)` — fill every cell with the given char/attributes
- `fillEmpty()` — reset every cell to blank
- `asString(): String` — concatenate all non-empty chars left to right

---

## `TerminalBuffer` — Full API

### Constructor

```kotlin
TerminalBuffer(
    width: Int,
    height: Int,
    maxScrollbackSize: Int = 1000
)
```

- `width` and `height` must be `>= 1`; throw `IllegalArgumentException` otherwise.
- `maxScrollbackSize` must be `>= 0`.
- Screen initializes to `height` blank lines, each of `width` blank cells.
- Scrollback initializes empty.
- Cursor starts at `(col=0, row=0)`.
- Current attributes start as `TextAttributes()` (all defaults).

---

### Attributes

#### `setAttributes(attributes: TextAttributes)`
Sets current attributes. Used by all subsequent write/fill operations.

---

### Cursor

#### `getCursorCol(): Int`
#### `getCursorRow(): Int`
Returns current cursor column / row (0-indexed, screen coordinates).

#### `setCursor(col: Int, row: Int)`
Sets cursor position. Clamps to valid bounds: `col` in `[0, width-1]`,
`row` in `[0, height-1]`.

#### `moveCursorUp(n: Int)`
Move cursor up by `n`. Clamps at row 0. `n` must be `>= 0`.

#### `moveCursorDown(n: Int)`
Move cursor down by `n`. Clamps at `height - 1`. `n` must be `>= 0`.

#### `moveCursorLeft(n: Int)`
Move cursor left by `n`. Clamps at col 0. `n` must be `>= 0`.

#### `moveCursorRight(n: Int)`
Move cursor right by `n`. Clamps at `width - 1`. `n` must be `>= 0`.

**Edge cases to test:**
- Move by 0 — cursor does not change
- Move by amount larger than screen — cursor lands on boundary exactly
- Move from boundary in that direction — cursor stays on boundary

---

### Editing (cursor-aware, attribute-aware)

All editing operations use the **current cursor position** as the starting
point and the **current attributes** for any cells written.

#### `writeText(text: String)`

Write `text` starting at cursor position, **overwriting** existing content.
- Each character replaces the cell at the current column; cursor advances right.
- When the cursor reaches `width`, it **wraps** to column 0 of the next row.
- When the cursor would move past row `height - 1` after writing the last char,
  the screen **scrolls up by one line**: the top line of the screen is pushed
  into scrollback (subject to max scrollback size), all other lines shift up,
  and a new blank line appears at the bottom. Cursor stays on `height - 1`.
- Empty string: no-op (cursor does not move).

**Edge cases:**
- Text that fills exactly one line
- Text that wraps across multiple lines
- Text that causes multiple scroll events
- Single character at last cell of screen (triggers scroll)
- Writing over previously written content

#### `insertText(text: String)`

Insert `text` at cursor position, **pushing existing content right**.
- Characters at and after the cursor shift right to make room.
- Characters pushed off the right edge of the line are **lost** (no line wrapping
  of displaced content — this matches typical terminal insert mode behavior).
- After insertion, cursor advances past the inserted text (clamped to width).
- Wrapping rule: if inserting would advance cursor past `width - 1`, cursor
  clamps to `width - 1`.

**Edge cases:**
- Insert at start of non-empty line
- Insert at end of line (same as write)
- Insert text longer than remaining space on line

#### `fillLine(row: Int, char: Char)`

Fill every cell of line `row` (screen coordinates) with `char` using current
attributes. Does **not** move the cursor. `row` must be in `[0, height-1]`.

**Variant**: `fillLineEmpty(row: Int)` — fill with blank cells (resets attributes too).

---

### Editing (cursor-independent)

#### `insertEmptyLineAtBottom()`

- Appends a new blank line at the bottom of the screen.
- The current top line of the screen is pushed into scrollback.
- If scrollback is at max capacity, the oldest scrollback line is dropped.
- Screen dimensions stay the same (still `height` lines).

#### `clearScreen()`

- Replaces all screen lines with blank lines.
- Scrollback is **not** affected.
- Cursor position is **not** reset.

#### `clearAll()`

- Clears both screen and scrollback.
- Cursor position is **not** reset.

---

### Content Access

All positional getters accept both **screen coordinates** and **scrollback coordinates**.

Define a `BufferPosition` that distinguishes:
- `Screen(row: Int, col: Int)` — `row` in `[0, height-1]`
- `Scrollback(row: Int, col: Int)` — `row` in `[0, scrollbackSize-1]`,
  where row 0 is the **oldest** line (farthest from screen top)

Or alternatively, expose scrollback via negative row indices — document
your choice clearly.

#### `getChar(position): Char`
Returns the character at `position`. Returns `' '` for empty cells.

#### `getAttributes(position): TextAttributes`
Returns attributes at `position`.

#### `getLine(row, fromScrollback): String`
Returns the line as a string. Trailing empty cells should be trimmed
(i.e., return the meaningful content width, not always `width` chars).

#### `getScreenContent(): String`
Returns all screen lines joined by `\n`. Trailing whitespace per line
should be trimmed.

#### `getFullContent(): String`
Returns scrollback + screen joined by `\n`, scrollback first (oldest at top).

---

## Wide Characters (Bonus)

Wide characters (e.g., `'中'`, `'😀'`) occupy **2 terminal columns**.

Detection: use `Character.getType(codePoint)` and check against
`Character.OTHER_LETTER` combined with Unicode East Asian Width property,
or simply use `ch.code > 0xFF && isWide(ch)` with a helper. A pragmatic
approach: treat any char where `ch >= '\u1100'` and it falls in a known
wide range as wide (CJK Unified Ideographs: `\u4E00`–`\u9FFF`, etc.).

Behavior:
- Writing a wide char at column `c` occupies cells `c` and `c+1`.
- Cell `c` gets the character; cell `c+1` gets a placeholder `Cell` with
  `char = '\u0000'` and `isEmpty = false`.
- If a wide char would start at `width - 1` (only one cell left), it should
  either wrap to the next line or be rejected — document your choice.
- Cursor advances by 2 after writing a wide char.
- `getLine()` should skip placeholder cells when building the string.

---

## Resize (Bonus)

#### `resize(newWidth: Int, newHeight: Int)`

Change screen dimensions. Content handling is a design decision — document it.

Suggested strategies:
- **Truncate/pad approach**: Lines wider than `newWidth` are truncated; lines
  shorter are padded with blank cells. Screen gains blank lines at bottom if
  taller, loses lines from bottom (pushed to scrollback) if shorter.
- **Reflow approach**: All content is re-wrapped at the new width. More complex
  but more realistic to real terminal behavior.

Either is acceptable — clearly justify your choice in `SOLUTION_NOTES.md`.

Cursor is clamped to new bounds after resize.

---

## Edge Case Reference

| Operation | Edge Case |
|-----------|-----------|
| `writeText` | empty string, exactly fills line, wraps multiple times, triggers scroll |
| `insertText` | at col 0, at col width-1, text longer than line |
| `fillLine` | row 0, row height-1, invalid row (should throw) |
| `moveCursor*` | n=0, n > dimension, already at boundary |
| `setCursor` | col/row at 0, at max, above max (clamped) |
| `insertEmptyLineAtBottom` | scrollback at max (oldest evicted), empty scrollback |
| `clearScreen` | after writing, check scrollback untouched |
| `getChar` | out-of-bounds position (should throw or return ' ') |
| `getLine` | empty line, full line, trailing spaces trimmed |
