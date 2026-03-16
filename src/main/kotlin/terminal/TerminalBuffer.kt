package terminal

import terminal.model.TextAttributes

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollbackSize: Int = 1000
) {
    private val screen: ArrayDeque<TerminalLine>
    private val scrollback: ArrayDeque<TerminalLine>

    private var cursorCol: Int = 0
    private var cursorRow: Int = 0

    var currentAttributes: TextAttributes = TextAttributes()
        private set

    init {
        require(width >= 1) { "Width must be at least 1, got $width" }
        require(height >= 1) { "Height must be at least 1, got $height" }
        require(maxScrollbackSize >= 0) { "Max scrollback size must be >= 0, got $maxScrollbackSize" }

        screen = ArrayDeque(height)
        repeat(height) {
            screen.addLast(TerminalLine(width))
        }
        scrollback = ArrayDeque()
    }

    fun setAttributes(attributes: TextAttributes) {
        currentAttributes = attributes
    }

    fun getCursorCol(): Int = cursorCol

    fun getCursorRow(): Int = cursorRow

    fun setCursor(col: Int, row: Int) {
        cursorCol = col.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveCursorUp(n: Int) {
        require(n >= 0) { "n must be >= 0, got $n" }
        cursorRow = (cursorRow - n).coerceAtLeast(0)
    }

    fun moveCursorDown(n: Int) {
        require(n >= 0) { "n must be >= 0, got $n" }
        cursorRow = (cursorRow + n).coerceAtMost(height - 1)
    }

    fun moveCursorLeft(n: Int) {
        require(n >= 0) { "n must be >= 0, got $n" }
        cursorCol = (cursorCol - n).coerceAtLeast(0)
    }

    fun moveCursorRight(n: Int) {
        require(n >= 0) { "n must be >= 0, got $n" }
        cursorCol = (cursorCol + n).coerceAtMost(width - 1)
    }
}
