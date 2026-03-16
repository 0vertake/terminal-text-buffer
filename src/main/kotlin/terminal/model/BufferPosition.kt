package terminal.model

sealed class BufferPosition {
    data class Screen(val row: Int, val col: Int) : BufferPosition()
    data class Scrollback(val row: Int, val col: Int) : BufferPosition()
}
