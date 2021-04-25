package com.athaydes.geminix.util.internal;

import com.athaydes.geminix.text.GemTextLine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static com.athaydes.geminix.util.internal.SpecialCharacters.URL_ENCODED_AMPERSAND;
import static com.athaydes.geminix.util.internal.SpecialCharacters.URL_ENCODED_EQUALS;

public final class UriHelper {

    private static final Pattern URI_PATTERN = Pattern.compile("([a-z]+)://.*");

    public static URI appendQuery(URI target, String userAnswer) {
        String query = target.getQuery();
        if (query == null || query.isEmpty()) {
            query = "";
        } else {
            query += URL_ENCODED_AMPERSAND;
        }

        query += "q" + URL_ENCODED_EQUALS + userAnswer;

        try {
            return new URI(target.getScheme(), null, target.getHost(), target.getPort(),
                    target.getPath(), query, target.getFragment());
        } catch (URISyntaxException e) {
            // error here should never happen
            return target;
        }
    }

    public static URI geminify(String uri) throws URISyntaxException {
        var matcher = URI_PATTERN.matcher(uri);
        if (matcher.matches()) {
            var scheme = matcher.group(1);
            if (!"gemini".equals(scheme)) {
                throw new IllegalArgumentException("Scheme must be gemini but was: " + scheme);
            }
        } else {
            uri = "gemini://" + uri;
        }

        var target = new URI(uri);
        var port = toGeminiPortIfNone(target);

        return new URI("gemini", null, target.getHost(), port,
                target.getRawPath(), target.getRawQuery(), target.getRawFragment());
    }

    public static URI appendLink(URI uri,
                                 GemTextLine.Link link) throws URISyntaxException {
        var target = link.url();

        // target is absolute path
        if (target.startsWith("/")) {
            return URI.create(uri.getScheme() + "://" + uri.getHost() +
                    ":" + toGeminiPortIfNone(uri) + target);
        }

        // target is full URI
        if (URI_PATTERN.matcher(target).matches()) {
            return geminify(target);
        }

        // target is relative path
        return URI.create(uri.getScheme() + "://" + uri.getHost() +
                ":" + toGeminiPortIfNone(uri) + joinPaths(uri.getPath(), target));
    }

    private static int toGeminiPortIfNone(URI target) {
        return (target.getPort() < 0) ? 1965 : target.getPort();
    }

    private static String joinPaths(String p1, String p2) {
        if (!p1.startsWith("/")) {
            p1 = "/" + p1;
        }
        if (p1.endsWith("/")) {
            p1 = p1.substring(0, p1.length() - 1);
        }
        if (p2.startsWith("/")) {
            return p1 + p2;
        }
        return p1 + "/" + p2;
    }
}
