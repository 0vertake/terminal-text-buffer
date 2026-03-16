package terminal

import kotlin.math.min
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
            if (isWide(char)) {
                if (width == 1) {
                    writeNormalChar(char)
                    advanceCursorBy(1)
                    continue
                }

                if (cursorCol == width - 1) {
                    advanceCursorBy(1)
                }

                writeWideChar(char)
                advanceCursorBy(2)
            } else {
                writeNormalChar(char)
                advanceCursorBy(1)
            }
        }
    }

    fun insertText(text: String) {
        if (text.isEmpty()) {
            return
        }

        val line = screenLine(cursorRow)
        val insertLength = min(text.length, width - cursorCol)
        if (insertLength <= 0) {
            return
        }

        for (col in (width - 1) downTo (cursorCol + insertLength)) {
            line.setCell(col, line.getCell(col - insertLength))
        }

        for (i in 0 until insertLength) {
            line.setCell(
                cursorCol + i,
                Cell(char = text[i], isEmpty = false, attributes = currentAttributes, charWidth = 1)
            )
        }

        cursorCol = (cursorCol + insertLength).coerceAtMost(width - 1)
    }

    fun fillLine(row: Int, char: Char) {
        val line = screenLine(row)
        line.fill(char, currentAttributes)
    }

    fun fillLineEmpty(row: Int) {
        val line = screenLine(row)
        line.fillEmpty()
    }

    fun insertEmptyLineAtBottom() {
        scrollUp()
    }

    fun clearScreen() {
        screen.forEach { it.fillEmpty() }
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun getChar(position: BufferPosition): Char {
        val cell = when (position) {
            is BufferPosition.Screen -> screenLine(position.row).getCell(position.col)
            is BufferPosition.Scrollback -> scrollbackLine(position.row).getCell(position.col)
        }
        return if (cell.isEmpty) ' ' else cell.char
    }

    fun getAttributes(position: BufferPosition): TextAttributes {
        return when (position) {
            is BufferPosition.Screen -> screenLine(position.row).getCell(position.col).attributes
            is BufferPosition.Scrollback -> scrollbackLine(position.row).getCell(position.col).attributes
        }
    }

    fun getLine(row: Int, fromScrollback: Boolean): String {
        return if (fromScrollback) {
            scrollbackLine(row).asString()
        } else {
            screenLine(row).asString()
        }
    }

    fun getScreenContent(): String {
        return screen.joinToString("\n") { it.asString() }
    }

    fun getFullContent(): String {
        val lines = ArrayList<String>(scrollback.size + screen.size)
        scrollback.forEach { lines.add(it.asString()) }
        screen.forEach { lines.add(it.asString()) }
        return lines.joinToString("\n")
    }

    private fun writeNormalChar(char: Char) {
        val line = screenLine(cursorRow)
        line.setCell(
            cursorCol,
            Cell(char = char, isEmpty = false, attributes = currentAttributes, charWidth = 1)
        )
    }

    private fun writeWideChar(char: Char) {
        val line = screenLine(cursorRow)
        line.setCell(
            cursorCol,
            Cell(char = char, isEmpty = false, attributes = currentAttributes, charWidth = 2)
        )
        line.setCell(
            cursorCol + 1,
            Cell(char = '\u0000', isEmpty = false, attributes = currentAttributes, charWidth = 1)
        )
    }

    private fun advanceCursorBy(steps: Int) {
        repeat(steps) {
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

    private fun scrollbackLine(row: Int): TerminalLine {
        require(row in 0 until scrollback.size) { "Row must be in 0 until ${scrollback.size}, got $row" }
        return scrollback.elementAt(row)
    }

    private fun isWide(char: Char): Boolean {
        val code = char.code
        return code in 0x1100..0x115F ||
            code in 0x2329..0x232A ||
            code in 0x2E80..0xA4CF ||
            code in 0xAC00..0xD7A3 ||
            code in 0xF900..0xFAFF ||
            code in 0xFE10..0xFE19 ||
            code in 0xFE30..0xFE6F ||
            code in 0xFF00..0xFF60 ||
            code in 0xFFE0..0xFFE6
    }
}
