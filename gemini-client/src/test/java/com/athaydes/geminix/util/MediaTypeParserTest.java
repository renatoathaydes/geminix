package com.athaydes.geminix.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaTypeParserTest {

    private final MediaTypeParser parser = new MediaTypeParser();

    static Stream<Object[]> canParseMediaType() {
        return Stream.of(
                new Object[]{"t", "t", "", Map.of(), false, false, false, false, false},
                new Object[]{"/v", "", "v", Map.of(), false, false, false, false, false},
                new Object[]{"text/plain", "text", "plain", Map.of(), true, false, false, false, false},
                new Object[]{"text/plain; charset=utf-8", "text", "plain", Map.of("charset", "utf-8"), true, false, false, false, false},
                new Object[]{"text/plain; charset = ascii; k = v", "text", "plain",
                        Map.of("charset", "ascii", "k", "v"), true, false, false, false, false},
                new Object[]{"image/jpg", "image", "jpg", Map.of(), false, false, true, false, false},
                new Object[]{"audio/MP3", "audio", "mp3", Map.of(), false, true, false, false, false},
                new Object[]{"application/json", "application", "json", Map.of(), false, false, false, true, false},
                new Object[]{"TEXT/gemini", "text", "gemini", Map.of(), true, false, false, false, true},
                new Object[]{"text/gemini;charset=UTF-16", "text", "gemini",
                        Map.of("charset", "UTF-16"), true, false, false, false, true}
        );
    }

    @ParameterizedTest
    @MethodSource
    void canParseMediaType(String input,
                           String type,
                           String subType,
                           Map<String, String> parameters,
                           boolean isText,
                           boolean isAudio,
                           boolean isImage,
                           boolean isApplication,
                           boolean isGeminiText) {
        var result = parser.parse(input);
        assertTrue(result.isPresent());
        var mediaType = result.get();
        assertEquals(type, mediaType.type());
        assertEquals(subType, mediaType.subType());
        assertEquals(parameters, mediaType.parameters());
        assertEquals(isText, mediaType.isText());
        assertEquals(isApplication, mediaType.isApplication());
        assertEquals(isAudio, mediaType.isAudio());
        assertEquals(isImage, mediaType.isImage());
        assertEquals(isGeminiText, mediaType.isGeminiText());
    }
}
