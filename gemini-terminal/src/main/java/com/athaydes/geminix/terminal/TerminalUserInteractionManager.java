package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.tls.TlsManager;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

public final class TerminalUserInteractionManager
        implements UserInteractionManager, Closeable, AutoCloseable {

    private final ErrorHandler errorHandler;
    private final TlsManager tlsManager;
    private final Terminal terminal;
    private final LineReader lineReader;
    private final TerminalPrinter printer;

    TerminalUserInteractionManager(TerminalPrinter terminalPrinter,
                                   TerminalErrorHandler terminalErrorHandler,
                                   CachedTlsCertificateStorage certificateStorage,
                                   CompleterFactory completerFactory) {
        this.printer = terminalPrinter;
        this.errorHandler = terminalErrorHandler;

        try {
            this.terminal = TerminalBuilder.builder()
                    .jansi(true)
                    .name("Geminix")
                    .encoding(StandardCharsets.UTF_8)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize terminal", e);
        }

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_FILE, Files.INSTANCE.getHistory())
                .completer(completerFactory.create())
                .appName("geminix")
                .build();

        this.tlsManager = new TerminalTlsManager(this, certificateStorage, printer);
    }

    @Override
    public TlsManager getTlsManager() {
        return tlsManager;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void beforeRequest(URI target) {
        printer.info("Sending request to: " + target);
    }

    @Override
    public void promptUser(String message, Predicate<String> acceptResponse) {
        var done = false;
        do {
            printer.prompt(message);
            var userResponse = lineReader.readLine(printer.prompt());
            done = acceptResponse.test(userResponse);
        } while (!done);
    }

    @Override
    public void showResponse(Response response) {
        printer.info("Response status: " + response.statusCode().name());

        if (response instanceof Response.Success success) {
            printer.info("Media Type: " + success.mediaType());
            if (success.mediaType().startsWith("text/")) {
                System.out.println();
                System.out.println(new String(success.body(), StandardCharsets.UTF_8));
            } else {
                printer.error("TODO : cannot yet handle non-textual media-type");
            }
        } else if (response instanceof Response.ClientCertRequired clientCertRequired) {
            printer.error("(client certificate is not yet supported) - " + clientCertRequired.userMessage());
        } else if (response instanceof Response.PermanentFailure failure) {
            printer.error(failure.errorMessage());
        } else if (response instanceof Response.TemporaryFailure failure) {
            printer.error(failure.errorMessage());
        }
    }

    @Override
    public void close() {
        getErrorHandler().run(() -> {
            terminal.close();
            return null;
        });
    }
}
