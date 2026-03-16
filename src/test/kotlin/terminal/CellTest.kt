package terminal.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CellTest {

    @Nested
    inner class BlankCellTests {

        @Test
        fun blankCell_hasExpectedDefaults() {
            val cell = Cell.blank()

            assertEquals(' ', cell.char)
            assertEquals(true, cell.isEmpty)
            assertEquals(TextAttributes(), cell.attributes)
            assertEquals(1, cell.charWidth)
        }
    }

    @Nested
    inner class CustomCellTests {

        @Test
        fun customCell_preservesProvidedValues() {
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(2),
                background = TerminalColor.Standard(4),
                style = TextStyle(bold = true, italic = false, underline = true)
            )

            val cell = Cell(
                char = 'X',
                isEmpty = false,
                attributes = attributes,
                charWidth = 1
            )

            assertEquals('X', cell.char)
            assertEquals(false, cell.isEmpty)
            assertEquals(attributes, cell.attributes)
            assertEquals(1, cell.charWidth)
        }

        @Test
        fun cell_supportsEquality() {
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(5),
                background = TerminalColor.Standard(6),
                style = TextStyle(bold = false, italic = true, underline = false)
            )

            val first = Cell('A', false, attributes, 1)
            val second = Cell('A', false, attributes, 1)

            assertEquals(first, second)
        }
    }
}
