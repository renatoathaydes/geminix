package com.athaydes.geminix.client;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.Preconditions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResponseParserTest {
    private final ResponseParser responseParser = new ResponseParser();

    @Test
    void canParseMinimalSuccessResponse() throws Exception {
        var is = new ByteArrayInputStream(new byte[]{'2', '0', ' ', '\r', '\n'});
        var result = responseParser.parse(is);
        assertEquals(StatusCode.SUCCESS_20, result.statusCode());
        assertEquals(Response.Success.class, result.getClass());
        assertEquals("", ((Response.Success) result).mediaType());
        assertEquals(-1, is.read());
    }

    @Test
    void canParseTypicalSuccessResponse() throws Exception {
        var is = new ByteArrayInputStream("20 text/gemini\r\nhello world".getBytes(StandardCharsets.UTF_8));
        var result = responseParser.parse(is);
        assertEquals(StatusCode.SUCCESS_20, result.statusCode());
        assertEquals(Response.Success.class, result.getClass());
        assertEquals("text/gemini", ((Response.Success) result).mediaType());
        assertSame(is, ((Response.Success) result).body());
        assertEquals("hello world", new String(is.readAllBytes(), StandardCharsets.US_ASCII));
    }

    @Test
    void canParseMetaWithMaxLength() throws Exception {
        var meta = IntStream.range(0, 1024).map(i -> i % 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());

        Preconditions.condition(meta.length() == 1024, "Meta has the maximum length permitted");

        var is = new ByteArrayInputStream(("59 " + meta + "\r\n").getBytes(StandardCharsets.UTF_8));
        var result = responseParser.parse(is);
        assertEquals(StatusCode.BAD_REQUEST_59, result.statusCode());
        assertEquals(Response.PermanentFailure.class, result.getClass());
        assertEquals(meta, ((Response.PermanentFailure) result).errorMessage());
    }

    @Test
    void cannotParseMetaWithOneByteTooMany() throws Exception {
        var meta = IntStream.range(0, 1025).map(i -> i % 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());

        Preconditions.condition(meta.length() == 1025, "Meta has the maximum length permitted + 1");

        var is = new ByteArrayInputStream(("59 " + meta + "\n").getBytes(StandardCharsets.UTF_8));
        var error = assertThrows(ResponseParseError.class, () -> responseParser.parse(is));
        assertEquals("Meta line is too long", error.getMessage());
    }
}
