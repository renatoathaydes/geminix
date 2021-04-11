package com.athaydes.geminix.protocol;

import com.athaydes.geminix.tls.TlsSocketFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.athaydes.geminix.SpecialCharacters.CRLF;
import static com.athaydes.geminix.util.UriHelper.appendQuery;

public class Client {

    private final UserInteractionManager userInteractionManager;
    private final TlsSocketFactory socketFactory;
    private final ResponseParser responseParser;

    public Client(UserInteractionManager userInteractionManager) {
        this(userInteractionManager, TlsSocketFactory.defaultFactory());
    }

    public Client(UserInteractionManager userInteractionManager, TlsSocketFactory socketFactory) {
        this(userInteractionManager, socketFactory, new ResponseParser());
    }

    public Client(UserInteractionManager userInteractionManager, TlsSocketFactory socketFactory, ResponseParser responseParser) {
        this.userInteractionManager = userInteractionManager;
        this.socketFactory = socketFactory;
        this.responseParser = responseParser;
    }

    public void sendRequest(URI target, ResponseErrorHandler errorHandler) {
        System.out.println("URI: " + target);
        errorHandler.run(() -> send(target)).ifPresent(response -> {
            if (response instanceof Response.Input input) {
                userInteractionManager.promptUser(input.prompt(), (userAnswer) -> {
                    var newTarget = appendQuery(target, userAnswer);
                    sendRequest(newTarget, errorHandler);
                });
            }
        });
    }

    private Response send(URI target) throws IOException, ResponseParseError {
        if (target.getUserInfo() != null) {
            throw new IllegalArgumentException("URI must not contain userInfo component");
        }
        try (var socket = socketFactory.create(target.getHost(), target.getPort())) {
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
