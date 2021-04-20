package com.athaydes.geminix.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GemTextParserTest {
    final GemTextParser parser = new GemTextParser();

    @Test
    void canParseText() {
        assertEquals(new GemTextLine.Text("hello world"), parser.parseLine("hello world"));
    }

    @Test
    void canParseH1() {
        assertEquals(new GemTextLine.Heading1("h1"), parser.parseLine("# h1"));
        assertEquals(new GemTextLine.Heading1(" h1111  "), parser.parseLine("#  h1111  "));
    }

    @Test
    void canParseH2() {
        assertEquals(new GemTextLine.Heading2("h2"), parser.parseLine("## h2"));
        assertEquals(new GemTextLine.Heading2(" h2 "), parser.parseLine("##  h2 "));
    }

    @Test
    void canParseH3() {
        assertEquals(new GemTextLine.Heading3("h3"), parser.parseLine("### h3"));
        assertEquals(new GemTextLine.Heading3(" h3!! "), parser.parseLine("###  h3!! "));
    }

    @Test
    void doesNotHaveH4() {
        assertEquals(new GemTextLine.Text("#### h4"), parser.parseLine("#### h4"));
    }

    @Test
    void doesNotAllowMissingWhitespaceInHeadings() {
        assertEquals(new GemTextLine.Text("#h"), parser.parseLine("#h"));
        assertEquals(new GemTextLine.Text("##i"), parser.parseLine("##i"));
        assertEquals(new GemTextLine.Text("###j"), parser.parseLine("###j"));
        assertEquals(new GemTextLine.Text("####kkkkkk"), parser.parseLine("####kkkkkk"));
    }

    @Test
    void canParseList() {
        assertEquals(new GemTextLine.ListItem("item 1"), parser.parseLine("* item 1"));
    }

    @Test
    void doesNotAllowMissingWhitespaceInList() {
        assertEquals(new GemTextLine.Text("*foo"), parser.parseLine("*foo"));
    }

    @Test
    void canParseLink() {
        assertEquals(new GemTextLine.Link("me", "Me at Gemini"),
                parser.parseLine("=> me Me at Gemini"));
        assertEquals(new GemTextLine.Link("/foo", ""),
                parser.parseLine("=>/foo"));
        assertEquals(new GemTextLine.Link("/bar", ""),
                parser.parseLine("=>    /bar"));
        assertEquals(new GemTextLine.Link("gemini://zort:1965/foo/bar", "  The ZORT"),
                parser.parseLine("=>    gemini://zort:1965/foo/bar   The ZORT  "));
    }

    @Test
    void canParsePreformattedStart() {
        assertEquals(new GemTextLine.PreformattedStart("dart"), parser.parseLine("```dart"));
    }

    @Test
    void canParsePreformattedBlock() {
        assertEquals(List.of(
                new GemTextLine.PreformattedStart(""),
                new GemTextLine.Preformatted("hello"),
                new GemTextLine.Preformatted("foo"),
                new GemTextLine.Preformatted("``"),
                new GemTextLine.Preformatted("  bar```"),
                GemTextLine.PreformattedEnd.INSTANCE,
                new GemTextLine.Text("last line")
        ), parser.apply(Stream.of("```", "hello", "foo", "``", "  bar```", "``` ignore", "last line")).toList());
    }
}
