package com.athaydes.geminix.browser.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class GeminiURL {
    public static void init() {
        URL.setURLStreamHandlerFactory(protocol -> {
            if ("gemini".equals(protocol)) {
                return geminiProtocolHandler();
            }
            return null;
        });
    }

    private static URLStreamHandler geminiProtocolHandler() {
        return new GeminiURLStreamHandler();
    }
}

final class GeminiURLStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new GeminiURLConnection(url);
    }

    @Override
    protected int getDefaultPort() {
        return 1965;
    }
}

final class GeminiURLConnection extends URLConnection {

    private static final List<String> htmlContentType = List.of("text/html; charset=utf-8");

    private byte[] data;

    public GeminiURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        data = "<h2>Hello Renato</h2>".getBytes(StandardCharsets.UTF_8);
        connected = true;
    }

    @Override
    public long getContentLengthLong() {
        return data.length;
    }

    @Override
    public String getContentType() {
        return htmlContentType.get(0);
    }

    @Override
    public long getDate() {
        return Instant.now().toEpochMilli();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return Map.of("Content-Type", htmlContentType);
    }
}