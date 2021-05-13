package com.athaydes.geminix.browser.internal;

import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.text.GemTextLine;
import com.athaydes.geminix.text.GemTextParser;
import com.athaydes.geminix.util.MediaType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextResponseReader {
    private final GemTextParser gemTextParser = new GemTextParser();

    public String readBody(MediaType mediaType, Response.Success success) {
        return streamBody(mediaType, success)
                .collect(Collectors.joining("\n"));
    }

    public Stream<String> streamBody(MediaType mediaType, Response.Success success) {
        var charsetText = mediaType.getParameter(MediaType.Params.CHARSET)
                .orElse(StandardCharsets.UTF_8.name());

        Charset charset;
        if (Charset.isSupported(charsetText)) {
            charset = Charset.forName(charsetText);
        } else {
//            printer.warn("Unsupported charset: '" + charsetText + "', will fallback to UTF-8.");
            charset = StandardCharsets.UTF_8;
        }

        var reader = new BufferedReader(new InputStreamReader(success.body(), charset), 1024);

        return gemTextParser.apply(reader.lines())
                .map(this::geminiTextToHtml);
    }

    private String geminiTextToHtml(GemTextLine line) {
        if (line instanceof GemTextLine.Text text) {
            return "<p>" + text.value() + "</p>";
        }
        if (line instanceof GemTextLine.PreformattedStart) {
            return "<pre>";
        }
        if (line instanceof GemTextLine.Preformatted pre) {
            return pre.value();
        }
        if (line instanceof GemTextLine.PreformattedEnd) {
            return "</pre>";
        }
        if (line instanceof GemTextLine.ListItem item) {
            return "<ul><li>" + item.value() + "</li></ul>";
        }
        if (line instanceof GemTextLine.Quote quote) {
            return "<quote>" + quote.value() + "</quote>";
        }
        if (line instanceof GemTextLine.Heading1 h1) {
            return "<h1>" + h1.value() + "</h1>";
        }
        if (line instanceof GemTextLine.Heading2 h2) {
            return "<h2>" + h2.value() + "</h2>";
        }
        if (line instanceof GemTextLine.Heading3 h3) {
            return "<h3>" + h3.value() + "</h3>";
        }
        if (line instanceof GemTextLine.Link link) {
            var description = link.description().isBlank() ? link.url() : link.description();
            return "<a href=\"" + link.url() + "\">" + description + "</a>";
        }
        return "";
    }
}
