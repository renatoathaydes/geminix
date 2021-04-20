package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.tls.FileTlsCertificateStorage;

import java.io.IOException;

public class Objects {
    private static final TerminalPrinter printer = new TerminalPrinter();
    private static final TerminalErrorHandler errorHandler = new TerminalErrorHandler(printer);
    private static final CachedTlsCertificateStorage certificateStorage = new CachedTlsCertificateStorage(
            new FileTlsCertificateStorage(Files.INSTANCE.getCertificates()), errorHandler);
    private static final BookmarksManager bookmarks = new BookmarksManager(
            Files.INSTANCE.getBookmarks(), printer);
    private static final CompleterFactory completerFactory = new CompleterFactory(certificateStorage, bookmarks);

     static final TerminalUserInteractionManager uim = new TerminalUserInteractionManager(
            printer, errorHandler, certificateStorage, completerFactory);

    static final Client client = new Client(uim);

    static final CommandHandler commandHandler = new CommandHandler(
            certificateStorage, printer, errorHandler, bookmarks, uim);

    static {
        try {
            bookmarks.load();
        } catch (IOException e) {
            printer.error("Could not load bookmarks from " + bookmarks.getFile() + " due to: " + e);
        }
    }

}
