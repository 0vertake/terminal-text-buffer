package terminal

import terminal.model.Cell

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

    fun fillEmpty() {
        for (i in cells.indices) {
            cells[i] = Cell.blank()
        }
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
