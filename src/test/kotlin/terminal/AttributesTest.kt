package terminal.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AttributesTest {

    @Nested
    inner class TextStyleTests {

        @Test
        fun textStyle_defaultsToAllFalse() {
            val style = TextStyle()

            assertEquals(TextStyle(bold = false, italic = false, underline = false), style)
        }

        @Test
        fun textStyle_allowsCustomFlags() {
            val style = TextStyle(bold = true, italic = true, underline = false)

            assertEquals(true, style.bold)
            assertEquals(true, style.italic)
            assertEquals(false, style.underline)
        }

        @Test
        fun textStyle_supportsEquality() {
            val first = TextStyle(bold = true, italic = false, underline = true)
            val second = TextStyle(bold = true, italic = false, underline = true)

            assertEquals(first, second)
        }
    }

    @Nested
    inner class TextAttributesTests {

        @Test
        fun textAttributes_defaultsToTerminalDefaults() {
            val attributes = TextAttributes()

            assertEquals(TerminalColor.Default, attributes.foreground)
            assertEquals(TerminalColor.Default, attributes.background)
            assertEquals(TextStyle(), attributes.style)
        }

        @Test
        fun textAttributes_allowsCustomValues() {
            val style = TextStyle(bold = true, italic = false, underline = true)
            val attributes = TextAttributes(
                foreground = TerminalColor.Standard(3),
                background = TerminalColor.Standard(12),
                style = style
            )

            assertEquals(TerminalColor.Standard(3), attributes.foreground)
            assertEquals(TerminalColor.Standard(12), attributes.background)
            assertEquals(style, attributes.style)
        }

        @Test
        fun textAttributes_supportsEquality() {
            val style = TextStyle(bold = false, italic = true, underline = true)
            val first = TextAttributes(
                foreground = TerminalColor.Standard(1),
                background = TerminalColor.Standard(2),
                style = style
            )
            val second = TextAttributes(
                foreground = TerminalColor.Standard(1),
                background = TerminalColor.Standard(2),
                style = style
            )

            assertEquals(first, second)
        }
    }
}
