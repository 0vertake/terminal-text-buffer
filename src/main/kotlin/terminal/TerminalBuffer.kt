package terminal

import terminal.model.BufferPosition
import terminal.model.Cell
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

    fun writeText(text: String) {
        if (text.isEmpty()) {
            return
        }

        for (char in text) {
            val line = screenLine(cursorRow)
            line.setCell(
                cursorCol,
                Cell(char = char, isEmpty = false, attributes = currentAttributes, charWidth = 1)
            )

            advanceCursorWithScroll()
        }
    }

    fun getChar(position: BufferPosition): Char {
        val cell = when (position) {
            is BufferPosition.Screen -> screenLine(position.row).getCell(position.col)
            is BufferPosition.Scrollback -> throw IllegalArgumentException("Scrollback access not supported yet")
        }
        return if (cell.isEmpty) ' ' else cell.char
    }

    fun getAttributes(position: BufferPosition): TextAttributes {
        return when (position) {
            is BufferPosition.Screen -> screenLine(position.row).getCell(position.col).attributes
            is BufferPosition.Scrollback -> throw IllegalArgumentException("Scrollback access not supported yet")
        }
    }

    fun getLine(row: Int, fromScrollback: Boolean): String {
        require(!fromScrollback) { "Scrollback access not supported yet" }
        return screenLine(row).asString()
    }

    fun getScreenContent(): String {
        return screen.joinToString("\n") { it.asString() }
    }

    private fun advanceCursorWithScroll() {
        cursorCol += 1
        if (cursorCol >= width) {
            cursorCol = 0
            cursorRow += 1
        }

        if (cursorRow >= height) {
            scrollUp()
            cursorRow = height - 1
        }
    }

    private fun scrollUp() {
        val removed = screen.removeFirst()
        scrollback.addLast(removed.copy())
        if (scrollback.size > maxScrollbackSize) {
            scrollback.removeFirst()
        }
        screen.addLast(TerminalLine(width))
    }

    private fun screenLine(row: Int): TerminalLine {
        require(row in 0 until height) { "Row must be in 0 until $height, got $row" }
        return screen.elementAt(row)
    }
}
