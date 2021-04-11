package com.athaydes.geminix.util;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class UriHelperTest {
    @Test
    public void uriCanBeGeminified() {
        var examples = new String[][]{
                // expected result, input
                {"gemini://gemini.circumlunar.space:1965", "gemini.circumlunar.space"},
                {"gemini://gemini.circumlunar.space:1965/docs/", "gemini.circumlunar.space/docs/"},
                {"gemini://gemini.circumlunar.space:1966/docs/", "gemini.circumlunar.space:1966/docs/"},
                {"gemini://gemini.circumlunar.space:1966/docs/", "gemini://gemini.circumlunar.space:1966/docs/"},
        };

        for (String[] example : examples) {
            assertEquals(
                    URI.create(example[0]),
                    UriHelper.geminify(example[1]));
        }
    }
}
