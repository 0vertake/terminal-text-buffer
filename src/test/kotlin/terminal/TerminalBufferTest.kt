package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.model.TerminalColor
import terminal.model.TextAttributes
import terminal.model.TextStyle

class TerminalBufferTest {

    @Nested
    inner class AttributesTests {

        @Test
        fun setAttributes_updatesCurrentAttributes() {
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollbackSize = 10)
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(4),
                background = TerminalColor.Standard(7),
                style = TextStyle(bold = true, italic = false, underline = true)
            )

            buffer.setAttributes(attributes)

            assertEquals(attributes, buffer.currentAttributes)
        }
    }
}
