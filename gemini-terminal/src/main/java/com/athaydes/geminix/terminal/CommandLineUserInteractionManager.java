package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Consumer;

public final class CommandLineUserInteractionManager implements UserInteractionManager {
    static final CommandLineUserInteractionManager INSTANCE = new CommandLineUserInteractionManager();

    private final Map<String, byte[]> certificatePublicKeyByHost = new HashMap<>();
    private State state;

    private CommandLineUserInteractionManager() {
    }

    @Override
    public void beforeRequest(URI target) {
        System.out.println("Sending request to: " + target);
    }

    @Override
    public void promptUser(String message, Consumer<String> onResponse) {
        System.out.println("PROMPT: " + message);
        var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        var userResponse = scanner.nextLine();
        onResponse.accept(userResponse);
    }

    @Override
    public void showResponse(Response response) {
        System.out.println("Response status: " + response.statusCode().name());

        if (response instanceof Response.Success success) {
            System.out.println("Media Type: " + success.mediaType());
            if (success.mediaType().startsWith("text/")) {
                System.out.println(new String(success.body(), StandardCharsets.UTF_8));
            }
        } else if (response instanceof Response.ClientCertRequired clientCertRequired) {
            System.out.println("ERROR: " + clientCertRequired.userMessage());
        } else if (response instanceof Response.PermanentFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        } else if (response instanceof Response.TemporaryFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public <T> Optional<T> run(Action<T> action) {
        try {
            return Optional.ofNullable(action.run());
        } catch (Exception exception) {
            error(exception.toString());
            return Optional.empty();
        }
    }

    @Override
    public void handleCertificate(X509Certificate certificate, String certificateStatus, String hostStatus) {
        var encodedKey = certificate.getPublicKey().getEncoded();
        var subject = certificate.getSubjectX500Principal().getName();
        var cachedPubKey = certificatePublicKeyByHost.get(subject);

        if (cachedPubKey != null && certificateStatus.isEmpty() && hostStatus.isEmpty()) {
            if (Arrays.equals(encodedKey, cachedPubKey)) {
                System.out.println("TLS Certificate approved");
                return;
            } else {
                System.out.println("ERROR: TLS Certificate for this host has been modified!");
            }
        }

        System.out.println("-------------------------");
        System.out.println("NEW Certificate: " + certificate);
        System.out.println("Status: " + (certificateStatus.isEmpty() ? "OK" : certificateStatus));
        System.out.println("Host: " + (hostStatus.isEmpty() ? "OK" : hostStatus));
        System.out.println("-------------------------");

        promptUser("Do you want to accept the above certificate? [y/n]", (answer) -> {
            if (answer.toLowerCase(Locale.ROOT).trim().equals("y")) {
                certificatePublicKeyByHost.put(subject, encodedKey);
            } else {
                System.out.println("Aborting request");
                throw new RuntimeException("Server Certificate was not accepted");
            }
        });
    }

    static void error(String message) {
        System.err.println("ERROR: " + message);
    }

}
