package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsSocketFactory;
import com.athaydes.geminix.util.UriHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static com.athaydes.geminix.util.SpecialCharacters.CRLF;
import static com.athaydes.geminix.util.UriHelper.appendQuery;

public class Client {

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
        userInteractionManager.getResponseErrorHandler().run(() -> {
            sendRequest(UriHelper.geminify(uri));
            return null;
        });
    }

    private void sendRequest(URI target) {
        userInteractionManager.getResponseErrorHandler().run(() -> send(target)).ifPresent(response -> {
            if (response instanceof Response.Input input) {
                userInteractionManager.promptUser(input.prompt(), (userAnswer) -> {
                    var newTarget = appendQuery(target, userAnswer);
                    sendRequest(newTarget);
                });
            } else if (response instanceof Response.Redirect redirect) {
                try {
                    sendRequest(UriHelper.geminify(redirect.uri()));
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Redirect URI is not valid: " + redirect.uri());
                }
            } else {
                userInteractionManager.showResponse(response);
            }
        });
    }

    private Response send(URI target) throws IOException, ResponseParseError {
        if (target.getUserInfo() != null) {
            throw new IllegalArgumentException("URI must not contain userInfo component");
        }

        userInteractionManager.beforeRequest(target);

        try (var socket = socketFactory.create(target.getHost(), target.getPort(),
                userInteractionManager.getTlsManager())) {
            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            sendLine(target, out);
            return responseParser.parse(in);
        }
    }

    private static void sendLine(Object object, OutputStream out) throws IOException {
        out.write(object.toString().getBytes(StandardCharsets.UTF_8));
        out.write(CRLF);
    }

}
