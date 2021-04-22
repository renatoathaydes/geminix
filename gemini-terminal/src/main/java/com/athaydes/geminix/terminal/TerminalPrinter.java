package com.athaydes.geminix.terminal;

import com.athaydes.geminix.text.GemTextLine;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

final class TerminalPrinter {
    private boolean enabled = true;
    private Color promptColor = MAGENTA;
    private Color infoColor = MAGENTA;
    private Color warnColor = YELLOW;
    private Color errorColor = RED;
    private Color linkColor = CYAN;
    private Color h1Color = BLUE;
    private Color h2Color = BLUE;
    private Color h3Color = BLUE;
    private Color quoteColor = DEFAULT;
    private Color listColor = DEFAULT;
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

    void print(GemTextLine line) {
        if (line instanceof GemTextLine.Link link) {
            print("→ " + link.url() + " " + link.description(), linkColor);
        } else if (line instanceof GemTextLine.Quote quote) {
            print("  " + ansi().bold().a(quote.value()).boldOff(), quoteColor);
        } else if (line instanceof GemTextLine.Heading1 h1) {
            print(ansi().bold().a("# " + h1.value()).boldOff().toString(), h1Color);
        } else if (line instanceof GemTextLine.Heading2 h2) {
            print(ansi().bold().a("## " + h2.value()).boldOff().toString(), h2Color);
        } else if (line instanceof GemTextLine.Heading3 h3) {
            print(ansi().bold().a("### " + h3.value()).boldOff().toString(), h3Color);
        } else if (line instanceof GemTextLine.ListItem listItem) {
            print("◘ " + listItem.value(), listColor);
        } else if (line instanceof  GemTextLine.PreformattedStart preStart) {
            System.out.println("``` " + preStart.altText());
        } else if (line instanceof GemTextLine.PreformattedEnd) {
            System.out.println("```");
        } else if (line instanceof GemTextLine.Preformatted pre) {
            System.out.println(pre.value());
        } else if (line instanceof GemTextLine.Text text) {
            System.out.println(text.value());
        }
    }

    void print(String message, Color color) {
        System.out.println(enabled
                ? ansi().fg(color).a(message).reset()
                : message);
    }
}
