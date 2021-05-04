package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.text.GemTextLine;
import com.athaydes.geminix.text.GemTextParser;
import com.athaydes.geminix.tls.TlsManager;
import com.athaydes.geminix.util.MediaType;
import com.athaydes.geminix.util.MediaTypeParser;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static com.athaydes.geminix.terminal.Objects.MAX_LINKS;

public final class TerminalUserInteractionManager
        implements UserInteractionManager, Closeable, AutoCloseable {

    private final ErrorHandler errorHandler;
    private final TlsManager tlsManager;
    private final Terminal terminal;
    private final LineReader lineReader;
    private final TerminalPrinter printer;
    private final MediaTypeParser mediaTypeParser;
    private final GemTextParser gemTextParser;

    private URI requestedUrl;
    private URI currentUrl;
    private final List<GemTextLine.Link> links;

    TerminalUserInteractionManager(TerminalPrinter terminalPrinter,
                                   TerminalErrorHandler terminalErrorHandler,
                                   CachedTlsCertificateStorage certificateStorage,
                                   CompleterFactory completerFactory,
                                   List<GemTextLine.Link> links) {
        this.printer = terminalPrinter;
        this.errorHandler = terminalErrorHandler;
        this.mediaTypeParser = new MediaTypeParser();
        this.gemTextParser = new GemTextParser();
        this.links = links;

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

    public URI getCurrentUrl() {
        return currentUrl;
    }

    public List<GemTextLine.Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    @Override
    public void beforeRequest(URI target) {
        requestedUrl = target;
        printer.info("Sending request to: " + target);
    }

    @Override
    public void promptUser(String message, Predicate<String> acceptResponse) {
        var done = false;
        do {
            if (!message.isBlank()) printer.prompt(message);
            var userResponse = lineReader.readLine(printer.prompt());
            done = acceptResponse.test(userResponse);
        } while (!done);
    }

    @Override
    public void showResponse(Response response) {
        printer.info("Response status: " + response.statusCode().name());

        if (response instanceof Response.Success success) {
            printer.info("Media Type: " + success.mediaType());
            var mediaType = mediaTypeParser
                    .parse(success.mediaType())
                    .orElse(MediaType.GEMINI_TEXT);
            if (mediaType.isText()) {
                showSuccessText(mediaType, success);
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

    private void showSuccessText(MediaType mediaType,
                                 Response.Success success) {
        mediaType.getParameter(MediaType.Params.LANGUAGE).ifPresent(lang -> {
            var language = Locale.forLanguageTag(lang).getDisplayName();
            if (!language.isEmpty() && !language.equals(lang)) {
                printer.info("Document language: " + language);
            }
        });

        var charsetText = mediaType.getParameter(MediaType.Params.CHARSET)
                .orElse(StandardCharsets.UTF_8.name());

        Charset charset;
        if (Charset.isSupported(charsetText)) {
            charset = Charset.forName(charsetText);
        } else {
            printer.warn("Unsupported charset: '" + charsetText + "', will fallback to UTF-8.");
            charset = StandardCharsets.UTF_8;
        }

        var reader = new BufferedReader(new InputStreamReader(success.body(), charset), 1024);

        System.out.println();

        if (mediaType.isGeminiText()) {
            currentUrl = requestedUrl;
            links.clear();
            gemTextParser.apply(reader.lines()).forEach(line -> {
                if (links.size() < MAX_LINKS && line instanceof GemTextLine.Link link) {
                    printer.print(link, links.size());
                    links.add(link);
                } else {
                    printer.print(line);
                }
            });
        } else {
            reader.lines().forEach(printer::print);
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
