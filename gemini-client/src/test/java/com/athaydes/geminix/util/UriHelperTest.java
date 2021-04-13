package com.athaydes.geminix.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UriHelperTest {

    record URIExample(String expectedGeminifiedURI, String example) {
    }

    @SuppressWarnings("unused")
    static Stream<URIExample> uriCanBeGeminified() {
        return Stream.of(
                new URIExample("gemini://gemini.circumlunar.space:1965",
                        "gemini.circumlunar.space"),
                new URIExample("gemini://gemini.circumlunar.space:1965/docs/",
                        "gemini.circumlunar.space/docs/"),
                new URIExample("gemini://gemini.circumlunar.space:1966/docs/",
                        "gemini.circumlunar.space:1966/docs/"),
                new URIExample("gemini://gemini.circumlunar.space:1966/docs/",
                        "gemini://gemini.circumlunar.space:1966/docs/")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void uriCanBeGeminified(URIExample sample) throws URISyntaxException {
        assertEquals(URI.create(sample.expectedGeminifiedURI), UriHelper.geminify(sample.example()));
    }
}
