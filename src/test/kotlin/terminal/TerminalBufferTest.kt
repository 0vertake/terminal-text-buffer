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

    @Nested
    inner class InsertTextTests {

        @Test
        fun insertText_insertsAtStartAndShiftsExistingContent() {
            val buffer = createBuffer()
            buffer.writeText("abc")
            buffer.setCursor(0, 0)

            buffer.insertText("Z")

            assertEquals("Zabc", buffer.getLine(row = 0, fromScrollback = false))
        }

        @Test
        fun insertText_insertsInMiddleAndDropsOverflow() {
            val buffer = createBuffer()
            buffer.writeText("abcde")
            buffer.setCursor(2, 0)

            buffer.insertText("Z")

            assertEquals("abZcd", buffer.getLine(row = 0, fromScrollback = false))
        }

        @Test
        fun insertText_insertsAtEndWithoutShifting() {
            val buffer = createBuffer()
            buffer.writeText("abcd")
            buffer.setCursor(4, 0)

            buffer.insertText("Z")

            assertEquals("abcdZ", buffer.getLine(row = 0, fromScrollback = false))
        }

        @Test
        fun insertText_truncatesWhenTextExceedsRemainingSpace() {
            val buffer = createBuffer()
            buffer.writeText("abcde")
            buffer.setCursor(3, 0)

            buffer.insertText("XYZ")

            assertEquals("abcXY", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals(4, buffer.getCursorCol())
        }
    }

    @Nested
    inner class FillLineTests {

        @Test
        fun fillLine_fillsFirstRowWithChar() {
            val buffer = createBuffer()

            buffer.fillLine(0, 'X')

            assertEquals("XXXXX", buffer.getLine(row = 0, fromScrollback = false))
        }

        @Test
        fun fillLine_fillsLastRowWithChar() {
            val buffer = createBuffer()

            buffer.fillLine(2, 'Y')

            assertEquals('Y', buffer.getChar(BufferPosition.Screen(row = 2, col = 4)))
        }

        @Test
        fun fillLine_usesCurrentAttributes() {
            val buffer = createBuffer()
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(1),
                background = TerminalColor.Standard(2),
                style = TextStyle(bold = true, italic = false, underline = true)
            )
            buffer.setAttributes(attributes)

            buffer.fillLine(1, 'Z')

            assertEquals(attributes, buffer.getAttributes(BufferPosition.Screen(row = 1, col = 0)))
        }

        @Test
        fun fillLineEmpty_resetsRowToBlank() {
            val buffer = createBuffer()
            buffer.fillLine(1, 'Q')

            buffer.fillLineEmpty(1)

            assertEquals("", buffer.getLine(row = 1, fromScrollback = false))
        }

        @Test
        fun fillLine_rejectsInvalidRow() {
            val buffer = createBuffer()

            assertThrows(IllegalArgumentException::class.java) {
                buffer.fillLine(3, 'X')
            }
        }
    }

    @Nested
    inner class InsertEmptyLineTests {

        @Test
        fun insertEmptyLineAtBottom_pushesTopLineToScrollback() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 5)
            buffer.fillLine(0, 'A')
            buffer.fillLine(1, 'B')

            buffer.insertEmptyLineAtBottom()

            assertEquals("BBB\n", buffer.getScreenContent())
            val scrollback = scrollbackLines(buffer)
            assertEquals(1, scrollback.size)
            assertEquals("AAA", scrollback[0].asString())
        }

        @Test
        fun insertEmptyLineAtBottom_evictionHonorsScrollbackCapacity() {
            val buffer = TerminalBuffer(width = 2, height = 2, maxScrollbackSize = 1)
            buffer.fillLine(0, 'A')
            buffer.fillLine(1, 'B')

            buffer.insertEmptyLineAtBottom()
            buffer.fillLine(1, 'C')
            buffer.insertEmptyLineAtBottom()

            val scrollback = scrollbackLines(buffer)
            assertEquals(1, scrollback.size)
            assertEquals("BB", scrollback[0].asString())
        }
    }

    @Nested
    inner class ScrollbackTests {

        @Test
        fun writeText_scrollsSingleLineWhenPassingBottom() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 10)

            buffer.writeText("abcdefg")

            assertEquals("def\ng", buffer.getScreenContent())
            val scrollback = scrollbackLines(buffer)
            assertEquals(1, scrollback.size)
            assertEquals("abc", scrollback[0].asString())
        }

        @Test
        fun writeText_scrollsMultipleTimes() {
            val buffer = TerminalBuffer(width = 2, height = 2, maxScrollbackSize = 10)

            buffer.writeText("abcdef")

            val scrollback = scrollbackLines(buffer)
            assertEquals(2, scrollback.size)
            assertEquals("ab", scrollback[0].asString())
            assertEquals("cd", scrollback[1].asString())
            assertEquals("ef\n", buffer.getScreenContent())
        }

        @Test
        fun writeText_evictionHonorsScrollbackCapacity() {
            val buffer = TerminalBuffer(width = 2, height = 2, maxScrollbackSize = 1)

            buffer.writeText("abcdef")

            val scrollback = scrollbackLines(buffer)
            assertEquals(1, scrollback.size)
            assertEquals("cd", scrollback[0].asString())
        }

        @Test
        fun writeText_keepsCursorOnLastRowAfterScroll() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 5)

            buffer.writeText("abcdef")

            assertEquals(1, buffer.getCursorRow())
            assertEquals(0, buffer.getCursorCol())
        }
    }

    private fun createBuffer(): TerminalBuffer = TerminalBuffer(width = 5, height = 3, maxScrollbackSize = 10)

    @Suppress("UNCHECKED_CAST")
    private fun scrollbackLines(buffer: TerminalBuffer): List<TerminalLine> {
        val field = TerminalBuffer::class.java.getDeclaredField("scrollback")
        field.isAccessible = true
        val deque = field.get(buffer) as ArrayDeque<TerminalLine>
        return deque.toList()
    }
}
