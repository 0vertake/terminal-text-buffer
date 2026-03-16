package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.model.BufferPosition
import terminal.model.TerminalColor
import terminal.model.TextAttributes
import terminal.model.TextStyle

class TerminalBufferTest {

    @Nested
    inner class AttributesTests {

        @Test
        fun setAttributes_updatesCurrentAttributes() {
            val buffer = createBuffer()
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(4),
                background = TerminalColor.Standard(7),
                style = TextStyle(bold = true, italic = false, underline = true)
            )

            buffer.setAttributes(attributes)

            assertEquals(attributes, buffer.currentAttributes)
        }
    }

    @Nested
    inner class CursorTests {

        @Test
        fun cursor_startsAtOrigin() {
            val buffer = createBuffer()

            assertEquals(0, buffer.getCursorCol())
            assertEquals(0, buffer.getCursorRow())
        }

        @Test
        fun setCursor_clampsToBounds() {
            val buffer = createBuffer()

            buffer.setCursor(-5, 99)

            assertEquals(0, buffer.getCursorCol())
            assertEquals(2, buffer.getCursorRow())
        }

        @Test
        fun moveCursorUp_clampsAtTop() {
            val buffer = createBuffer()
            buffer.setCursor(1, 1)

            buffer.moveCursorUp(5)

            assertEquals(0, buffer.getCursorRow())
        }

        @Test
        fun moveCursorDown_clampsAtBottom() {
            val buffer = createBuffer()

            buffer.moveCursorDown(99)

            assertEquals(2, buffer.getCursorRow())
        }

        @Test
        fun moveCursorLeft_clampsAtLeft() {
            val buffer = createBuffer()
            buffer.setCursor(1, 0)

            buffer.moveCursorLeft(5)

            assertEquals(0, buffer.getCursorCol())
        }

        @Test
        fun moveCursorRight_clampsAtRight() {
            val buffer = createBuffer()

            buffer.moveCursorRight(99)

            assertEquals(4, buffer.getCursorCol())
        }

        @Test
        fun moveCursorByZero_doesNotChangePosition() {
            val buffer = createBuffer()
            buffer.setCursor(2, 1)

            buffer.moveCursorUp(0)
            buffer.moveCursorDown(0)
            buffer.moveCursorLeft(0)
            buffer.moveCursorRight(0)

            assertEquals(2, buffer.getCursorCol())
            assertEquals(1, buffer.getCursorRow())
        }

        @Test
        fun moveCursorUp_rejectsNegativeValues() {
            val buffer = createBuffer()

            assertThrows(IllegalArgumentException::class.java) {
                buffer.moveCursorUp(-1)
            }
        }
    }

    @Nested
    inner class ContentAccessTests {

        @Test
        fun getChar_returnsSpaceForBlankCell() {
            val buffer = createBuffer()

            val char = buffer.getChar(BufferPosition.Screen(row = 0, col = 0))

            assertEquals(' ', char)
        }

        @Test
        fun getAttributes_returnsDefaultForBlankCell() {
            val buffer = createBuffer()

            val attributes = buffer.getAttributes(BufferPosition.Screen(row = 1, col = 2))

            assertEquals(TextAttributes(), attributes)
        }

        @Test
        fun getLine_returnsEmptyStringForBlankLine() {
            val buffer = createBuffer()

            val line = buffer.getLine(row = 0, fromScrollback = false)

            assertEquals("", line)
        }

        @Test
        fun getScreenContent_returnsJoinedBlankLines() {
            val buffer = createBuffer()

            val content = buffer.getScreenContent()

            assertEquals("\n\n", content)
        }

        @Test
        fun getLine_rejectsScrollbackAccessBeforeImplemented() {
            val buffer = createBuffer()

            assertThrows(IllegalArgumentException::class.java) {
                buffer.getLine(row = 0, fromScrollback = true)
            }
        }

        @Test
        fun getChar_rejectsScrollbackAccessBeforeImplemented() {
            val buffer = createBuffer()

            assertThrows(IllegalArgumentException::class.java) {
                buffer.getChar(BufferPosition.Scrollback(row = 0, col = 0))
            }
        }
    }

    @Nested
    inner class WriteTextTests {

        @Test
        fun writeText_writesFromCursorPosition() {
            val buffer = createBuffer()
            buffer.setCursor(1, 1)

            buffer.writeText("Hi")

            assertEquals('H', buffer.getChar(BufferPosition.Screen(row = 1, col = 1)))
            assertEquals('i', buffer.getChar(BufferPosition.Screen(row = 1, col = 2)))
        }

        @Test
        fun writeText_wrapsToNextLineWhenExceedingWidth() {
            val buffer = createBuffer()
            buffer.setCursor(3, 0)

            buffer.writeText("abcd")

            assertEquals('a', buffer.getChar(BufferPosition.Screen(row = 0, col = 3)))
            assertEquals('b', buffer.getChar(BufferPosition.Screen(row = 0, col = 4)))
            assertEquals('c', buffer.getChar(BufferPosition.Screen(row = 1, col = 0)))
            assertEquals('d', buffer.getChar(BufferPosition.Screen(row = 1, col = 1)))
            assertEquals(2, buffer.getCursorCol())
            assertEquals(1, buffer.getCursorRow())
        }

        @Test
        fun writeText_emptyStringDoesNotMoveCursor() {
            val buffer = createBuffer()
            buffer.setCursor(2, 2)

            buffer.writeText("")

            assertEquals(2, buffer.getCursorCol())
            assertEquals(2, buffer.getCursorRow())
        }

        @Test
        fun writeText_usesCurrentAttributes() {
            val buffer = createBuffer()
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(2),
                background = TerminalColor.Standard(3),
                style = TextStyle(bold = true, italic = true, underline = false)
            )
            buffer.setAttributes(attributes)

            buffer.writeText("X")

            assertEquals(attributes, buffer.getAttributes(BufferPosition.Screen(row = 0, col = 0)))
        }
    }

    private fun createBuffer(): TerminalBuffer = TerminalBuffer(width = 5, height = 3, maxScrollbackSize = 10)
}
