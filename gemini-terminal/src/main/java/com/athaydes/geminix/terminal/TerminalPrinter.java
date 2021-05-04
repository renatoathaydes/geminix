package com.athaydes.geminix.terminal;

import com.athaydes.geminix.text.GemTextLine;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

final class TerminalPrinter {
    private boolean enabled = true;
    private int maxTextWidth = 120;
    Color promptColor = MAGENTA;
    Color infoColor = MAGENTA;
    Color warnColor = YELLOW;
    Color errorColor = RED;
    Color linkColor = CYAN;
    Color h1Color = BLUE;
    Color h2Color = BLUE;
    Color h3Color = BLUE;
    Color quoteColor = DEFAULT;
    Color listColor = DEFAULT;
    private String prompt = "> ";

    public int getMaxTextWidth() {
        return maxTextWidth;
    }

    public void setMaxTextWidth(int maxTextWidth) {
        this.maxTextWidth = maxTextWidth;
    }

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

    public void setLinkColor(Color linkColor) {
        this.linkColor = linkColor;
    }

    public void setH1Color(Color h1Color) {
        this.h1Color = h1Color;
    }

    public void setH2Color(Color h2Color) {
        this.h2Color = h2Color;
    }

    public void setH3Color(Color h3Color) {
        this.h3Color = h3Color;
    }

    public void setQuoteColor(Color quoteColor) {
        this.quoteColor = quoteColor;
    }

    public void setListColor(Color listColor) {
        this.listColor = listColor;
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

    void print(GemTextLine.Link link, int index) {
        var desc = link.description().isBlank() ? link.url() : link.description();
        print("[" + index + "] → " + desc, linkColor, Ansi.Attribute.UNDERLINE);
    }

    void print(GemTextLine line) {
        if (line instanceof GemTextLine.Text text) {
            print(text.value());
        } else if (line instanceof GemTextLine.Link link) {
            print("→ " + link.url() + " " + link.description(), linkColor, Ansi.Attribute.UNDERLINE);
        } else if (line instanceof GemTextLine.Quote quote) {
            print("  " + quote.value(), quoteColor);
        } else if (line instanceof GemTextLine.Heading1 h1) {
            print("# " + h1.value(), h1Color, Ansi.Attribute.INTENSITY_BOLD);
        } else if (line instanceof GemTextLine.Heading2 h2) {
            print("## " + h2.value(), h2Color, Ansi.Attribute.INTENSITY_BOLD);
        } else if (line instanceof GemTextLine.Heading3 h3) {
            print("### " + h3.value(), h3Color, Ansi.Attribute.INTENSITY_BOLD);
        } else if (line instanceof GemTextLine.ListItem listItem) {
            print("◘ " + listItem.value(), listColor);
        } else if (line instanceof GemTextLine.PreformattedStart preStart) {
            print("``` " + preStart.altText());
        } else if (line instanceof GemTextLine.PreformattedEnd) {
            print("```");
        } else if (line instanceof GemTextLine.Preformatted pre) {
            print(pre.value());
        }
    }

    void print(String message) {
        printWithLimitedWidth(System.out, message, maxTextWidth);
    }

    void print(String message, Color color) {
        printWithLimitedWidth(System.out, message, maxTextWidth, color);
    }

    void print(String message, Color color, Ansi.Attribute... attributes) {
        printWithLimitedWidth(System.out, message, maxTextWidth, color, attributes);
    }

    void printWithLimitedWidth(PrintStream out, String text, int width) {
        printWithLimitedWidth(out, text, width, null);
    }

    void printWithLimitedWidth(PrintStream out, String text, int width, Color color, Ansi.Attribute... attributes) {
        if (text.isEmpty()) {
            out.println();
            return;
        }
        final var breakWordWidthLimit = width - Math.min(12, width / 3);
        var start = 0;
        var end = width;
        var len = text.length();
        while (start < len) {
            end = Math.min(len, end);
            var isSpaceAtEnd = end > 1 && text.charAt(end - 1) == ' ';
            if (!isSpaceAtEnd) {
                var isSpaceAfterEnd = text.length() > end && text.charAt(end) == ' ';
                if (!isSpaceAfterEnd) {
                    // try to find a space from the end so we can keep words together
                    var spaceIdx = text.lastIndexOf(' ', end);
                    if (spaceIdx >= start + breakWordWidthLimit) {
                        end = spaceIdx;
                    }
                }
            }

            var message = text.substring(start, end).trim();
            out.println(color != null && enabled ? format(message, color, attributes) : message);

            // ignore leading spaces in the next line
            start = indexOfNonSpace(text, end);
            end = start + width;
        }
    }

    private Ansi format(String message, Color color, Ansi.Attribute... attributes) {
        var ansi = ansi().fg(color).a(message);
        for (Ansi.Attribute attribute : attributes) {
            ansi.a(attribute);
        }
        return ansi.reset();
    }

    private static int indexOfNonSpace(String text, int from) {
        for (int i = from; i < text.length(); i++) {
            if (text.charAt(i) != ' ') {
                return i;
            }
        }
        return text.length();
    }
}
