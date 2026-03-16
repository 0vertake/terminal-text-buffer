package terminal

import terminal.model.Cell
import terminal.model.TextAttributes

class TerminalLine(val width: Int) {
    private val cells: Array<Cell>

    init {
        require(width >= 1) { "Width must be at least 1, got $width" }
        cells = Array(width) { Cell.blank() }
    }

    fun getCell(col: Int): Cell {
        ensureColInBounds(col)
        return cells[col]
    }

    fun setCell(col: Int, cell: Cell) {
        ensureColInBounds(col)
        cells[col] = cell
    }

    fun fill(char: Char, attributes: TextAttributes) {
        for (i in cells.indices) {
            cells[i] = Cell(char = char, isEmpty = false, attributes = attributes, charWidth = 1)
        }
    }

    fun fillEmpty() {
        for (i in cells.indices) {
            cells[i] = Cell.blank()
        }
    }

    fun asString(): String {
        val lastContentIndex = cells.indexOfLast { !it.isEmpty }
        if (lastContentIndex < 0) {
            return ""
        }

        val builder = StringBuilder(lastContentIndex + 1)
        for (i in 0..lastContentIndex) {
            val cell = cells[i]
            builder.append(if (cell.isEmpty) ' ' else cell.char)
        }
        return builder.toString()
    }

    fun copy(): TerminalLine {
        val copy = TerminalLine(width)
        for (i in cells.indices) {
            copy.setCell(i, cells[i])
        }
        return copy
    }

    private fun ensureColInBounds(col: Int) {
        require(col in 0 until width) { "Column must be in 0 until $width, got $col" }
    }
}
