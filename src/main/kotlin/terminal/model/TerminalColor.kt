package terminal.model

sealed class TerminalColor {
    object Default : TerminalColor()

    data class Standard(val index: Int) : TerminalColor() {
        init {
            require(index in 0..15) { "Color index must be 0..15, got $index" }
        }
    }
}
