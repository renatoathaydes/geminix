package com.athaydes.geminix.util;

import com.athaydes.geminix.text.GemTextLine.Link;
import com.athaydes.geminix.util.internal.UriHelper;
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

    static Stream<Object[]> canAppendLinkToUri() {
        return Stream.of(
                new Object[]{"gemini://hi.com", "foo", "gemini://hi.com:1965/foo"},
                new Object[]{"gemini://hi.com", "/foo", "gemini://hi.com:1965/foo"},
                new Object[]{"gemini://hi.com/foo", "bar", "gemini://hi.com:1965/foo/bar"},
                new Object[]{"gemini://hi.com/foo/", "bar", "gemini://hi.com:1965/foo/bar"},
                new Object[]{"gemini://hi.com/foo", "/bar", "gemini://hi.com:1965/bar"},
                new Object[]{"gemini://hi.com", "gemini://bye.com/", "gemini://bye.com:1965/"},
                new Object[]{"gemini://hi.com", "gemini://bye.com:1964/foo?a=1&b=2", "gemini://bye.com:1964/foo?a=1&b=2"},
                new Object[]{"gemini://hi.com/foo", "gemini://bye.com/zzz/", "gemini://bye.com:1965/zzz/"},
                new Object[]{"gemini://hi.com:1967/foo/", "/zzz/?x=1", "gemini://hi.com:1967/zzz/?x=1"},
                new Object[]{"gemini://hi.com:1967/foo/", "?x=1", "gemini://hi.com:1967/foo/?x=1"},
                new Object[]{"gemini://hi.com/", "http://foo.com", "http://foo.com"}
        );
    }

    @ParameterizedTest
    @MethodSource
    public void canAppendLinkToUri(String uri, String link, String expectedUri) throws URISyntaxException {
        assertEquals(URI.create(expectedUri), UriHelper.appendLink(URI.create(uri), new Link(link, "")));
    }
}
