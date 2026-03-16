package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.model.Cell
import terminal.model.TerminalColor
import terminal.model.TextAttributes
import terminal.model.TextStyle

class TerminalLineTest {

    @Nested
    inner class CellAccessTests {

        @Test
        fun getCell_returnsCellAtColumn() {
            val line = TerminalLine(3)
            val cell = Cell('A', isEmpty = false, attributes = TextAttributes(), charWidth = 1)

            line.setCell(2, cell)

            assertEquals(cell, line.getCell(2))
        }

        @Test
        fun setCell_acceptsBoundaryColumns() {
            val line = TerminalLine(2)
            val first = Cell('F', isEmpty = false, attributes = TextAttributes(), charWidth = 1)
            val last = Cell('L', isEmpty = false, attributes = TextAttributes(), charWidth = 1)

            line.setCell(0, first)
            line.setCell(1, last)

            assertEquals(first, line.getCell(0))
            assertEquals(last, line.getCell(1))
        }

        @Test
        fun getCell_throwsForNegativeColumn() {
            val line = TerminalLine(2)

            assertThrows(IllegalArgumentException::class.java) {
                line.getCell(-1)
            }
        }

        @Test
        fun setCell_throwsForColumnPastEnd() {
            val line = TerminalLine(2)

            assertThrows(IllegalArgumentException::class.java) {
                line.setCell(2, Cell.blank())
            }
        }
    }

    @Nested
    inner class FillTests {

        @Test
        fun fill_overwritesAllCellsWithAttributes() {
            val line = TerminalLine(4)
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(1),
                background = TerminalColor.Standard(2),
                style = TextStyle(bold = true, italic = false, underline = true)
            )

            line.fill('X', attributes)

            for (col in 0 until line.width) {
                val cell = line.getCell(col)
                assertEquals('X', cell.char)
                assertEquals(false, cell.isEmpty)
                assertEquals(attributes, cell.attributes)
                assertEquals(1, cell.charWidth)
            }
        }

        @Test
        fun fillEmpty_resetsCellsToBlank() {
            val line = TerminalLine(3)
            line.fill('Z', TextAttributes())

            line.fillEmpty()

            for (col in 0 until line.width) {
                assertEquals(Cell.blank(), line.getCell(col))
            }
        }
    }

    @Nested
    inner class AsStringTests {

        @Test
        fun asString_trimsTrailingEmptyCells() {
            val line = TerminalLine(4)
            line.setCell(0, Cell('A', isEmpty = false, attributes = TextAttributes(), charWidth = 1))

            assertEquals("A", line.asString())
        }

        @Test
        fun asString_retainsExplicitSpaces() {
            val line = TerminalLine(3)
            line.setCell(0, Cell('A', isEmpty = false, attributes = TextAttributes(), charWidth = 1))
            line.setCell(1, Cell(' ', isEmpty = false, attributes = TextAttributes(), charWidth = 1))

            assertEquals("A ", line.asString())
        }

        @Test
        fun asString_returnsEmptyForBlankLine() {
            val line = TerminalLine(5)

            assertEquals("", line.asString())
        }

        @Test
        fun asString_preservesInternalEmptyCellsAsSpaces() {
            val line = TerminalLine(4)
            line.setCell(0, Cell('A', isEmpty = false, attributes = TextAttributes(), charWidth = 1))
            line.setCell(3, Cell('B', isEmpty = false, attributes = TextAttributes(), charWidth = 1))

            assertEquals("A  B", line.asString())
        }
    }

    @Nested
    inner class CopyTests {

        @Test
        fun copy_createsIndependentLine() {
            val line = TerminalLine(2)
            line.setCell(0, Cell('Z', isEmpty = false, attributes = TextAttributes(), charWidth = 1))

            val copy = line.copy()
            line.setCell(0, Cell('X', isEmpty = false, attributes = TextAttributes(), charWidth = 1))

            assertEquals('Z', copy.getCell(0).char)
            assertEquals('X', line.getCell(0).char)
        }
    }
}
