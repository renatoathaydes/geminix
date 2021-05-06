package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.text.GemTextLine;
import com.athaydes.geminix.tls.FileTlsCertificateStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Objects {
    static final int MAX_LINKS = 1024;

    private static final List<GemTextLine.Link> links = new ArrayList<>(MAX_LINKS);

    private static final TerminalPrinter printer = new TerminalPrinter();
    private static final TerminalErrorHandler errorHandler = new TerminalErrorHandler(printer);
    private static final CachedTlsCertificateStorage certificateStorage = new CachedTlsCertificateStorage(
            new FileTlsCertificateStorage(Files.INSTANCE.getCertificates()), errorHandler);
    private static final BookmarksManager bookmarks = new BookmarksManager(
            Files.INSTANCE.getBookmarks(), printer);
    private static final CompleterFactory completerFactory = new CompleterFactory(
            certificateStorage, bookmarks, Collections.unmodifiableList(links));

    static final TerminalUserInteractionManager uim = new TerminalUserInteractionManager(
            printer, errorHandler, certificateStorage, completerFactory, links);

    static final Client client = new Client(uim);

    static final CommandHandler commandHandler = new CommandHandler(
            certificateStorage, printer, errorHandler, bookmarks, uim, client);

    static {
        try {
            bookmarks.load();
        } catch (IOException e) {
            printer.error("Could not load bookmarks from " + bookmarks.getFile() + " due to: " + e);
        }
    }

}
