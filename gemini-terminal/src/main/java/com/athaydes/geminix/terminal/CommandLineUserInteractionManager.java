package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.tls.TlsManager;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Predicate;

public final class CommandLineUserInteractionManager implements UserInteractionManager {

    static final CommandLineUserInteractionManager INSTANCE = new CommandLineUserInteractionManager();

    private final TlsManager tlsManager = new TerminalTlsManager(this);
    private final ErrorHandler errorHandler = new TerminalErrorHandler();

    private CommandLineUserInteractionManager() {
    }

    @Override
    public TlsManager getTlsManager() {
        return tlsManager;
    }

    @Override
    public ErrorHandler getResponseErrorHandler() {
        return errorHandler;
    }

    @Override
    public void beforeRequest(URI target) {
        System.out.println("Sending request to: " + target);
    }

    @Override
    public void promptUser(String message, Predicate<String> acceptResponse) {
        var done = false;
        do {
            System.out.println("PROMPT: " + message);
            var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            var userResponse = scanner.nextLine();
            done = acceptResponse.test(userResponse);
        } while (!done);
    }

    @Override
    public void showResponse(Response response) {
        System.out.println("Response status: " + response.statusCode().name());

        if (response instanceof Response.Success success) {
            System.out.println("Media Type: " + success.mediaType());
            if (success.mediaType().startsWith("text/")) {
                System.out.println(new String(success.body(), StandardCharsets.UTF_8));
            } else {
                System.out.println("TODO : cannot yet handle non-textual media-type");
            }
        } else if (response instanceof Response.ClientCertRequired clientCertRequired) {
            System.out.println("ERROR: " + clientCertRequired.userMessage());
        } else if (response instanceof Response.PermanentFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        } else if (response instanceof Response.TemporaryFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        }
    }

}
