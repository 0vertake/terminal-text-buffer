package terminal

import terminal.model.TextAttributes

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollbackSize: Int = 1000
) {
    private val screen: ArrayDeque<TerminalLine>
    private val scrollback: ArrayDeque<TerminalLine>

    var cursorCol: Int = 0
        private set
    var cursorRow: Int = 0
        private set

    var currentAttributes: TextAttributes = TextAttributes()
        private set

    init {
        require(width >= 1) { "Width must be at least 1, got $width" }
        require(height >= 1) { "Height must be at least 1, got $height" }
        require(maxScrollbackSize >= 0) { "Max scrollback size must be >= 0, got $maxScrollbackSize" }

        screen = ArrayDeque(height)
        repeat(height) {
            screen.addLast(TerminalLine(width))
        }
        scrollback = ArrayDeque()
    }
}
