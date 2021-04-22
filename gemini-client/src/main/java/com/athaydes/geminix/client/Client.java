package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsSocketFactory;
import com.athaydes.geminix.util.UriHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static com.athaydes.geminix.util.SpecialCharacters.CRLF;
import static com.athaydes.geminix.util.UriHelper.appendQuery;

public class Client {

    private static final int MAX_REDIRECTS_ALLOWED = 5;

    private final UserInteractionManager userInteractionManager;
    private final TlsSocketFactory socketFactory;
    private final ResponseParser responseParser;

    public Client(UserInteractionManager userInteractionManager) {
        this(userInteractionManager, TlsSocketFactory.defaultFactory());
    }

    public Client(UserInteractionManager userInteractionManager,
                  TlsSocketFactory socketFactory) {
        this(userInteractionManager, socketFactory, new ResponseParser());
    }

    public Client(UserInteractionManager userInteractionManager,
                  TlsSocketFactory socketFactory,
                  ResponseParser responseParser) {
        this.userInteractionManager = userInteractionManager;
        this.socketFactory = socketFactory;
        this.responseParser = responseParser;
    }

    public void sendRequest(String uri) {
        userInteractionManager.getErrorHandler().run(() -> {
            sendRequest(UriHelper.geminify(uri));
            return null;
        });
    }

    private void sendRequest(URI target) {
        var currentUri = new AtomicReference<>(target);
        userInteractionManager.getErrorHandler().run(() -> {
            var visitedURIs = new HashSet<URI>(2);
            while (true) {
                var response = send(currentUri.get());
                if (response instanceof Response.Input input) {
                    userInteractionManager.promptUser(input.prompt(), (userAnswer) -> {
                        currentUri.set(appendQuery(currentUri.get(), userAnswer));
                        return true;
                    });
                } else if (response instanceof Response.Redirect redirect) {
                    currentUri.set(handleRedirect(visitedURIs, redirect));
                } else {
                    try {
                        userInteractionManager.showResponse(response);
                    } finally {
                        // success responses keep a reference to the socket's stream and must be explicitly closed
                        if (response instanceof Response.Success success) {
                            success.body().close();
                        }
                    }

                    break;
                }
            }
            return null;
        });
    }

    protected URI handleRedirect(HashSet<URI> visitedURIs, Response.Redirect redirect) {
        URI newTarget;
        try {
            newTarget = UriHelper.geminify(redirect.uri());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Redirect URI '" + redirect.uri() + "' is not valid. " + e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Redirect URI '" + redirect.uri() + "' cannot be followed: " + e.getMessage());
        }
        var isNew = visitedURIs.add(newTarget);
        if (!isNew) {
            throw new RuntimeException("Redirect cycle detected for URI '" + newTarget +
                    "'. Already visited: " + visitedURIs);
        }
        if (visitedURIs.size() > MAX_REDIRECTS_ALLOWED) {
            throw new RuntimeException("Too many redirects. Visited URIs: " + visitedURIs);
        }
        return newTarget;
    }

    private Response send(URI target) throws IOException, ResponseParseError {
        if (target.getUserInfo() != null) {
            throw new IllegalArgumentException("URI must not contain userInfo component");
        }

        userInteractionManager.beforeRequest(target);
        var socket = socketFactory.create(
                target.getHost(), target.getPort(), userInteractionManager.getTlsManager());
        Response response = null;
        try {
            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            sendLine(target, out);
            response = responseParser.parse(in);
        } finally {
            // never close success responses here as they will contain the InputStream for reading the body
            if (!(response instanceof Response.Success)) {
                socket.close();
            }
        }
        return response;
    }

    private static void sendLine(Object object, OutputStream out) throws IOException {
        out.write(object.toString().getBytes(StandardCharsets.UTF_8));
        out.write(CRLF);
    }

}
