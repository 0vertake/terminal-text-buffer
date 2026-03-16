package terminal.model

data class TextAttributes(
    val foreground: TerminalColor = TerminalColor.Default,
    val background: TerminalColor = TerminalColor.Default,
    val style: TextStyle = TextStyle()
)
