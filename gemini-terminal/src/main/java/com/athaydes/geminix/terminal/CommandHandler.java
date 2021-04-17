package com.athaydes.geminix.terminal;

final class CommandHandler {
    private static final String HELP = """
            Geminix is a Gemini Client.
                        
            You can use it to navigate the Gemini space, much like you can use a web browser to navigate the WWW.
                        
            To visit a URL, simply type it in. The scheme and port are optional.
            The following are example URIs you can try:
                        
            => gemini.circumlunar.space/
            => geminispace.info/
            => gemini://tobykurien.com/
                        
            Geminix supports the following commands:
                        
            * help     - shows this help message.
            * quit     - quits Geminix.
                        
            To enter a command, enter a whitespace first (URLs cannot have whitespace).
                        
            """;

    private final TerminalPrinter printer;

    public CommandHandler(TerminalPrinter printer) {
        this.printer = printer;
    }

    /**
     * Handles a user command.
     *
     * @param answer user input
     * @return true to exit CLI loop
     */
    public boolean handle(String answer) {
        switch (answer) {
            case "help":
                printer.info(HELP);
                break;
            case "quit":
                return true;
            default:
                printer.error("Invalid command: " + answer);
        }
        return false;
    }
}
