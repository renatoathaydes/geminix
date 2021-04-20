package com.athaydes.geminix.text;

public sealed interface GemTextLine
        permits GemTextLine.Heading1, GemTextLine.Heading2, GemTextLine.Heading3,
        GemTextLine.Preformatted, GemTextLine.PreformattedStart, GemTextLine.PreformattedEnd,
        GemTextLine.Quote, GemTextLine.ListItem, GemTextLine.Link, GemTextLine.Text {

    final record Heading1(String value) implements GemTextLine {
    }

    final record Heading2(String value) implements GemTextLine {
    }

    final record Heading3(String value) implements GemTextLine {
    }

    final record Preformatted(String value) implements GemTextLine {
    }

    final record PreformattedStart(String altText) implements GemTextLine {
    }

    enum PreformattedEnd implements GemTextLine {
        INSTANCE
    }

    final record Quote(String value) implements GemTextLine {
    }

    final record ListItem(String value) implements GemTextLine {
    }

    final record Link(String url, String description) implements GemTextLine {
    }

    final record Text(String value) implements GemTextLine {
    }
}
