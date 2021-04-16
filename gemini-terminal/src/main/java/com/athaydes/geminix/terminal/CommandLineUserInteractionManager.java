package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.tls.FileTlsCertificateStorage;
import com.athaydes.geminix.tls.TlsManager;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Predicate;

public final class CommandLineUserInteractionManager
        implements UserInteractionManager, Closeable, AutoCloseable {

    static final CommandLineUserInteractionManager INSTANCE = new CommandLineUserInteractionManager();

    private final ErrorHandler errorHandler;
    private final TlsManager tlsManager;
    private final Terminal terminal;
    private final LineReader lineReader;

    private CommandLineUserInteractionManager() {
        this.errorHandler = new TerminalErrorHandler();

        try {
            this.terminal = TerminalBuilder.terminal();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize terminal", e);
        }

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("geminix")
                .build();

        var fileStorage = new FileTlsCertificateStorage(Paths.get("certs"));
        var tlsCertificateStorage = new CachedTlsCertificateStorage(fileStorage, errorHandler);

        this.tlsManager = new TerminalTlsManager(this, tlsCertificateStorage);
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
            System.out.println("GeminiX: " + message);
            var userResponse = lineReader.readLine("> ");
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
            System.out.println("ERROR: (client certificate is not yet supported) - " + clientCertRequired.userMessage());
        } else if (response instanceof Response.PermanentFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        } else if (response instanceof Response.TemporaryFailure failure) {
            System.out.println("ERROR: " + failure.errorMessage());
        }
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }
}
