# Solution Notes

## Design Decisions
- Used `TerminalColor` as a sealed class (`Default` vs `Standard`) to clearly distinguish "no color" from ANSI index 0.
- Modeled `Cell` with `isEmpty` plus a `blank()` factory so empty cells remain semantically different from explicit spaces with attributes.
- Implemented `TerminalLine` on top of `Array<Cell>` for O(1) access; `asString()` trims trailing empty cells but preserves internal spacing by inserting `' '` for empty cells between non-empty content, while skipping wide-char placeholders (`'\u0000'`).
- Built `TerminalBuffer` with `ArrayDeque` for both screen and scrollback to keep scroll operations O(1); scrollback stores deep copies of lines to avoid mutation of history.
- Implemented `writeText` to wrap on width and scroll on overflow, keeping the cursor on the last row after a scroll. Wide characters occupy two cells and emit a placeholder cell.
- Implemented `insertText` as a single-line, push-right insert that drops overflow, matching typical terminal insert behavior.
- Implemented `resize()` with a truncate/pad strategy: screen lines are resized to the new width, height shrink moves bottom lines into scrollback, and cursor is clamped.

## Trade-offs
- I kept the public API minimal and relied on internal inspection in tests for scrollback sizing; a production API would likely expose scrollback size explicitly.
- `getFullContent()` joins all lines (including empty lines), which can yield trailing newline characters. This keeps the method consistent with `getScreenContent()` semantics.
- Wide character detection uses a pragmatic Unicode range check on `Char`; it does not fully handle surrogate pairs or grapheme clusters.

## Known Limitations
- Surrogate-pair emoji are not handled as single wide characters; they will be processed per UTF-16 `Char`.
- No dedicated API for scrollback size or line count; clients must infer or track it externally.

## What I Would Improve With More Time
- Implement robust wide-character handling using code points and a full East Asian Width table.
- Add a resize reflow strategy option for more realistic terminal behavior.
- Expose scrollback size and bounds through public accessors to avoid reflective test access.
- Add more property-based tests for large inputs and randomized cursor movements.

## Thread Safety
- `TerminalBuffer` is not thread-safe; callers must synchronize externally if used across threads.

## Bonus Features
- Wide characters: implemented with a placeholder cell and a range-based width check.
- Resize: implemented with truncate/pad strategy and scrollback preservation on height shrink.
