package com.athaydes.geminix.browser.internal;

import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.util.MediaType;
import com.athaydes.geminix.util.MediaTypeParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GeminiURL {
    public static final record Dependencies(
            MediaTypeParser mediaTypeParser,
            TextResponseReader textResponseReader
    ) {
    }

    public static void init(Dependencies dependencies) {
        URL.setURLStreamHandlerFactory(protocol -> {
            if ("gemini".equals(protocol)) {
                return geminiProtocolHandler(dependencies);
            }
            return null;
        });
    }

    private static URLStreamHandler geminiProtocolHandler(Dependencies dependencies) {
        return new GeminiURLStreamHandler(dependencies);
    }
}

final class GeminiURLStreamHandler extends URLStreamHandler {
    private final GeminiURL.Dependencies dependencies;

    public GeminiURLStreamHandler(GeminiURL.Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    protected URLConnection openConnection(URL url) {
        return new GeminiURLConnection(url, dependencies);
    }

    @Override
    protected int getDefaultPort() {
        return 1965;
    }
}

final class GeminiURLConnection extends URLConnection {

    private static final List<String> htmlContentType = List.of("text/html; charset=utf-8");

    private InputStream inputStream;
    private Map<String, List<String>> headerFields;
    private long date;

    private final MediaTypeParser mediaTypeParser = new MediaTypeParser();
    private final GeminiURL.Dependencies dependencies;

    private final BrowserUserInteractionManager uim = new BrowserUserInteractionManager(response -> {
        if (response instanceof Response.Success success) {
            handleSuccess(success);
        } else {
            System.out.println("TODO : handle non-success response");
        }
    });

    private final Client client = new Client(uim);

    public GeminiURLConnection(URL url, GeminiURL.Dependencies dependencies) {
        super(url);
        this.dependencies = dependencies;

        // the body is streamed through to the UI async
        client.setAutoCloseSuccessResponseBody(false);
    }

    private void handleSuccess(Response.Success success) {
        date = Instant.now().toEpochMilli();
        var mediaType = mediaTypeParser.parse(success.mediaType()).orElse(MediaType.GEMINI_TEXT);
        System.out.println("SUCCESS with media-type " + mediaType);
        String contentType;
        if (mediaType.isGeminiText()) {
            // gemini text is converted to HTML, always using utf-8
            contentType = "text/html; charset=utf-8";
            var bodyStream = dependencies.textResponseReader().streamBody(mediaType, success);
            inputStream = new GeminiTextInputStream(bodyStream);
        } else {
            contentType = success.mediaType();
            inputStream = success.body();
        }
        headerFields = Map.of("Content-Type", List.of(contentType));
    }

    @Override
    public void connect() {
        if (connected) {
            return;
        }
        client.sendRequest(url.toString());
        connected = true;
    }

    @Override
    public long getContentLengthLong() {
        // content-length is never known in Gemini until we receive the full response
        return -1L;
    }

    @Override
    public String getContentType() {
        return htmlContentType.get(0);
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }
}

final class GeminiTextInputStream extends InputStream {

    private final Iterator<String> lines;

    private byte[] currentLine;
    private int currentIndex = 0;
    private int lineCount;

    GeminiTextInputStream(Stream<String> lines) {
        this.lines = lines.iterator();
    }

    @Override
    public int read() {
        if (currentLine == null || currentIndex >= currentLine.length) {
            if (lines.hasNext()) {
                currentLine = lines.next().getBytes(StandardCharsets.UTF_8);
                currentIndex = 0;
                lineCount++;
            } else {
                return -1;
            }
        }
        return currentLine[currentIndex++];
    }

    @Override
    public void close() throws IOException {
        System.out.println("Gemini Stream consumed lines: " + lineCount);
        super.close();
    }
}