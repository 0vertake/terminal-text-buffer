package terminal.model

data class Cell(
    val char: Char,
    val isEmpty: Boolean,
    val attributes: TextAttributes,
    val charWidth: Int = 1
) {
    companion object {
        fun blank(): Cell = Cell(
            char = ' ',
            isEmpty = true,
            attributes = TextAttributes(),
            charWidth = 1
        )
    }
}
