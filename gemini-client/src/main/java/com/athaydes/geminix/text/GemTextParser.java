package com.athaydes.geminix.text;

import java.util.function.Function;
import java.util.stream.Stream;

public class GemTextParser implements Function<Stream<String>, Stream<GemTextLine>> {

    private boolean isPreformatted = false;

    @Override
    public Stream<GemTextLine> apply(Stream<String> lines) {
        return lines.map(this::parseLine);
    }

    public void reset() {
        isPreformatted = false;
    }

    public GemTextLine parseLine(String line) {
        if (isPreformatted) {
            if (line.startsWith("```")) {
                isPreformatted = false;
                return GemTextLine.PreformattedEnd.INSTANCE;
            }
            return new GemTextLine.Preformatted(line);
        }
        if (line.startsWith("```")) {
            isPreformatted = true;
            var altText = line.substring(3);
            return new GemTextLine.PreformattedStart(altText);
        }
        if (line.startsWith("### ")) {
            return new GemTextLine.Heading3(line.substring(4));
        }
        if (line.startsWith("## ")) {
            return new GemTextLine.Heading2(line.substring(3));
        }
        if (line.startsWith("# ")) {
            return new GemTextLine.Heading1(line.substring(2));
        }
        if (line.startsWith("* ")) {
            return new GemTextLine.ListItem(line.substring(2));
        }
        if (line.startsWith(">")) {
            return new GemTextLine.Quote(line.substring(1));
        }
        if (line.startsWith("=>")) {
            var parts = line.substring(2).trim().split("\\s", 2);
            if (parts.length == 1) {
                return new GemTextLine.Link(parts[0].trim(), "");
            }
            if (parts.length == 2) {
                return new GemTextLine.Link(parts[0].trim(), parts[1]);
            }
        }
        return new GemTextLine.Text(line);
    }
}
