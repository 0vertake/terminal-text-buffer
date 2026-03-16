package terminal.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerminalColorTest {

    @Nested
    inner class StandardValidationTests {

        @Test
        fun standard_acceptsLowerBoundaryIndex() {
            val color = TerminalColor.Standard(0)

            assertEquals(0, color.index)
        }

        @Test
        fun standard_acceptsUpperBoundaryIndex() {
            val color = TerminalColor.Standard(15)

            assertEquals(15, color.index)
        }

        @Test
        fun standard_acceptsMiddleIndex() {
            val color = TerminalColor.Standard(7)

            assertEquals(7, color.index)
        }

        @Test
        fun standard_rejectsIndexBelowRange() {
            assertThrows(IllegalArgumentException::class.java) {
                TerminalColor.Standard(-1)
            }
        }

        @Test
        fun standard_rejectsIndexAboveRange() {
            assertThrows(IllegalArgumentException::class.java) {
                TerminalColor.Standard(16)
            }
        }
    }
}
