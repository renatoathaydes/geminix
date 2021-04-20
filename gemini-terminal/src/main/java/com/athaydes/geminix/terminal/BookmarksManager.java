package com.athaydes.geminix.terminal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class BookmarksManager {

    private final Path file;
    private final TerminalPrinter printer;
    private final Map<String, String> bookmarks = new HashMap<>();

    public BookmarksManager(Path file, TerminalPrinter printer) {
        this.file = file;
        this.printer = printer;
    }

    Path getFile() {
        return file;
    }

    Map<String, String> getAll() {
        return Collections.unmodifiableMap(bookmarks);
    }

    Optional<String> get(String name) {
        return Optional.ofNullable(bookmarks.get(name));
    }

    boolean add(String name, String url) throws IOException {
        if (bookmarks.containsKey(name)) {
            return false;
        }
        bookmarks.put(name, url);
        store(name, url);
        return true;
    }

    boolean remove(String name) throws IOException {
        var removed = bookmarks.remove(name) != null;
        if (removed) {
            writeOut();
        }
        return removed;
    }

    void load() throws IOException {
        if (!file.toFile().isFile()) {
            return;
        }
        Files.lines(file, StandardCharsets.UTF_8).forEach(line -> {
            if (line.isEmpty()) return;
            var parts = line.split("\\s+", 2);
            if (parts.length == 2) {
                bookmarks.put(parts[0], parts[1]);
            } else {
                printer.warn("Bookmarks file (" + file + ") invalid line: " + line);
            }
        });
    }

    private void writeOut() throws IOException {
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            for (Map.Entry<String, String> entry : bookmarks.entrySet()) {
                writer.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
            }
        }
    }

    private void store(String name, String url) throws IOException {
        var line = new StringBuilder(name.length() + url.length() + 2);
        line.append('\n').append(name).append(' ').append(url);
        Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
