package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IntegrationScenarioTest {

    @Nested
    inner class ScenarioTests {

        @Test
        fun writeParagraph_populatesScrollbackAndScreenInOrder() {
            val buffer = TerminalBuffer(width = 4, height = 2, maxScrollbackSize = 10)

            buffer.writeText("abcdefghij")

            assertEquals("abcd\nefgh\nij", buffer.getFullContent())
            assertEquals("efgh\nij", buffer.getScreenContent())
        }

        @Test
        fun cursorNavigation_allowsTargetedWrites() {
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollbackSize = 10)

            buffer.writeText("hello")
            buffer.setCursor(1, 1)
            buffer.writeText("X")
            buffer.setCursor(0, 2)
            buffer.moveCursorRight(3)
            buffer.writeText("Y")

            assertEquals("hello", buffer.getLine(row = 0, fromScrollback = false))
            assertEquals(" X", buffer.getLine(row = 1, fromScrollback = false))
            assertEquals("   Y", buffer.getLine(row = 2, fromScrollback = false))
        }

        @Test
        fun fillClearWrite_cycleResetsScreenThenWrites() {
            val buffer = TerminalBuffer(width = 4, height = 2, maxScrollbackSize = 10)

            buffer.fillLine(0, 'A')
            buffer.fillLine(1, 'B')
            buffer.clearScreen()
            buffer.writeText("hi")

            assertEquals("hi\n", buffer.getScreenContent())
        }

        @Test
        fun writeAndInsertEmptyLine_interleaveMaintainsScrollback() {
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollbackSize = 10)

            buffer.writeText("abc")
            buffer.insertEmptyLineAtBottom()
            buffer.writeText("def")

            assertEquals("abc\n\ndef\n", buffer.getFullContent())
        }
    }
}
