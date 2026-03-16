package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WideCharTest {

    private val wideChar = '\u4E2D'

    @Nested
    inner class WideCharWriteTests {

        @Test
        fun writeText_writesPlaceholderAndAdvancesCursor() {
            val buffer = TerminalBuffer(width = 4, height = 2, maxScrollbackSize = 5)

            buffer.writeText(wideChar.toString())

            val line = screenLines(buffer)[0]
            val mainCell = line.getCell(0)
            val placeholder = line.getCell(1)

            assertEquals(wideChar, mainCell.char)
            assertFalse(mainCell.isEmpty)
            assertEquals(2, mainCell.charWidth)
            assertEquals('\u0000', placeholder.char)
            assertFalse(placeholder.isEmpty)
            assertEquals(wideChar.toString(), buffer.getLine(row = 0, fromScrollback = false))
            assertEquals(2, buffer.getCursorCol())
            assertEquals(0, buffer.getCursorRow())
        }

        @Test
        fun writeText_wrapsWideCharWhenOnlyOneCellLeft() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 5)
            buffer.setCursor(2, 0)

            buffer.writeText(wideChar.toString())

            val line = screenLines(buffer)[1]
            val mainCell = line.getCell(0)
            val placeholder = line.getCell(1)

            assertEquals(wideChar, mainCell.char)
            assertEquals('\u0000', placeholder.char)
            assertEquals("", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals(wideChar.toString(), buffer.getLine(row = 1, fromScrollback = false))
            assertEquals(2, buffer.getCursorCol())
            assertEquals(1, buffer.getCursorRow())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun screenLines(buffer: TerminalBuffer): List<TerminalLine> {
        val field = TerminalBuffer::class.java.getDeclaredField("screen")
        field.isAccessible = true
        val deque = field.get(buffer) as ArrayDeque<TerminalLine>
        return deque.toList()
    }
}
