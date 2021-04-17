package com.athaydes.geminix.terminal;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.fusesource.jansi.Ansi.ansi;

final class TerminalPrinter {
    private Color promptColor = MAGENTA;
    private Color infoColor = MAGENTA;
    private Color warnColor = YELLOW;
    private Color errorColor = RED;
    private String prompt = "> ";

    String prompt() {
        return ansi().fg(promptColor).bold().a(prompt).boldOff().toString();
    }

    void prompt(String message) {
        print("< " + message, promptColor);
    }

    void info(String message) {
        print(message, infoColor);
    }

    void warn(String message) {
        print("WARN: " + message, warnColor);
    }

    void error(String message) {
        print("ERROR: " + message, errorColor);
    }

    void print(String message, Color color) {
        System.out.println(ansi().fg(color).a(message).reset());
    }
}
