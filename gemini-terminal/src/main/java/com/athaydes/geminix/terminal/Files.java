package com.athaydes.geminix.terminal;

import java.nio.file.Path;
import java.nio.file.Paths;

final class Files {
    static final Files INSTANCE = new Files();

    private final Path certificates;
    private final Path history;
    private final Path bookmarks;

    private Files() {
        var geminixHome = System.getenv().getOrDefault("GEMINIX_HOME",
                Paths.get(System.getProperty("user.home"), ".geminix").toString());
        certificates = Paths.get(geminixHome, "certs");
        history = Paths.get(geminixHome, "history");
        bookmarks = Paths.get(geminixHome, "bookmarks");
    }

    Path getCertificates() {
        return certificates;
    }

    Path getHistory() {
        return history;
    }

    Path getBookmarks() {
        return bookmarks;
    }
}
