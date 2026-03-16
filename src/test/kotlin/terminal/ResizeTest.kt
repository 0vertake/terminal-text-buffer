package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResizeTest {

    @Nested
    inner class ResizeScenarios {

        @Test
        fun resize_growsScreenAndPreservesContent() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 5)
            buffer.writeText("abc")

            buffer.resize(newWidth = 5, newHeight = 3)

            assertEquals(5, buffer.width)
            assertEquals(3, buffer.height)
            assertEquals("abc", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals("abc\n\n", buffer.getScreenContent())
        }

        @Test
        fun resize_shrinksScreenAndMovesTrimmedLinesToScrollback() {
            val buffer = TerminalBuffer(width = 2, height = 3, maxScrollbackSize = 5)
            buffer.fillLine(0, 'A')
            buffer.fillLine(1, 'B')
            buffer.fillLine(2, 'C')

            buffer.resize(newWidth = 2, newHeight = 2)

            assertEquals("AA", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals("BB", buffer.getLine(row = 1, fromScrollback = false))
            assertEquals("CC", buffer.getLine(row = 0, fromScrollback = true))
        }

        @Test
        fun resize_clampsCursorToNewBounds() {
            val buffer = TerminalBuffer(width = 3, height = 3, maxScrollbackSize = 5)
            buffer.setCursor(2, 2)

            buffer.resize(newWidth = 2, newHeight = 2)

            assertEquals(1, buffer.getCursorCol())
            assertEquals(1, buffer.getCursorRow())
        }

        @Test
        fun resize_growThenShrinkWidthPreservesTopLines() {
            val buffer = TerminalBuffer(width = 2, height = 2, maxScrollbackSize = 5)
            buffer.fillLine(0, 'A')
            buffer.fillLine(1, 'B')

            buffer.resize(newWidth = 4, newHeight = 2)
            buffer.resize(newWidth = 1, newHeight = 2)

            assertEquals("A", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals("B", buffer.getLine(row = 1, fromScrollback = false))
        }
    }
}
