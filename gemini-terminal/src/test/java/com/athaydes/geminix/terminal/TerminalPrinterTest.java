package com.athaydes.geminix.terminal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TerminalPrinterTest {
    private final TerminalPrinter printer = new TerminalPrinter();

    static Stream<Object[]> canPrintLimitedWidthLines() {
        return Stream.of(
                new Object[]{"", 5, List.of("")},
                new Object[]{"foo", 5, List.of("foo")},
                new Object[]{"foo", 2, List.of("fo", "o")},
                new Object[]{"foo bar foobar", 3, List.of("foo", "bar", "foo", "bar")},
                new Object[]{"foo    bar foobar", 3, List.of("foo", "bar", "foo", "bar")},
                new Object[]{"foo bar zortex a bcd", 6, List.of("foo ba", "r zort", "ex a", "bcd")},
                new Object[]{"abc def ghi jkl", 10, List.of("abc def", "ghi jkl")}
        );
    }

    @ParameterizedTest
    @MethodSource
    void canPrintLimitedWidthLines(String text, int width, List<String> expectedLines) {
        var out = Mockito.mock(PrintStream.class);
        var invocationCalls = new ArrayList<String>(expectedLines.size());
        Mockito.doAnswer(invocation -> {
            invocationCalls.add(invocation.getArgument(0).toString());
            return null;
        }).when(out).println(Mockito.anyString());
        Mockito.doAnswer(invocation -> {
            invocationCalls.add("");
            return null;
        }).when(out).println();

        printer.printWithLimitedWidth(out, text, width);

        assertEquals(expectedLines, invocationCalls);
    }
}
