package com.athaydes.geminix.terminal;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.fusesource.jansi.Ansi.ansi;

final class TerminalPrinter {
    private boolean enabled = true;
    private Color promptColor = MAGENTA;
    private Color infoColor = MAGENTA;
    private Color warnColor = YELLOW;
    private Color errorColor = RED;
    private String prompt = "> ";

    public void colors(boolean enable) {
        this.enabled = enable;
    }

    public void setPromptColor(Color promptColor) {
        this.promptColor = promptColor;
    }

    public void setInfoColor(Color infoColor) {
        this.infoColor = infoColor;
    }

    public void setWarnColor(Color warnColor) {
        this.warnColor = warnColor;
    }

    public void setErrorColor(Color errorColor) {
        this.errorColor = errorColor;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    String prompt() {
        return enabled
                ? ansi().fg(promptColor).bold().a(prompt).boldOff().toString()
                : prompt;
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
        System.out.println(enabled
                ? ansi().fg(color).a(message).reset()
                : message);
    }
}
