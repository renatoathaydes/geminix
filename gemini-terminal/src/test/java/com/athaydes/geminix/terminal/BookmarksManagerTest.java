package com.athaydes.geminix.terminal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookmarksManagerTest {

    @Test
    void canCreateBookmarksManagerFromNonExistingFile() throws IOException {
        var tmpDir = Files.createTempDirectory("BookmarksManager");
        var nonExistingFile = tmpDir.resolve("foo");
        var bookmarks = new BookmarksManager(nonExistingFile, new TerminalPrinter());

        bookmarks.load();

        assertIterableEquals(Map.of().entrySet(), bookmarks.getAll().entrySet());
        assertFalse(nonExistingFile.toFile().exists());
    }

    @Test
    void canCreateBookmarksManagerFromNonExistingFileThenAddEntry() throws IOException {
        var tmpDir = Files.createTempDirectory("BookmarksManager");
        var file = tmpDir.resolve("bm");
        var bookmarks = new BookmarksManager(file, new TerminalPrinter());

        bookmarks.load();

        var added = bookmarks.add("foo", "bar");

        assertTrue(added);
        assertTrue(file.toFile().exists());
        assertEquals("\nfoo bar", Files.readString(file));
    }

    @Test
    void canCreateBookmarksManagerFromExistingFileThenGetEntries() throws IOException {
        var tmpDir = Files.createTempDirectory("BookmarksManager");
        var file = tmpDir.resolve("bm2");
        Files.writeString(file, "\nfirst bookmark\n2 bookmarks", StandardOpenOption.CREATE_NEW);
        var bookmarks = new BookmarksManager(file, new TerminalPrinter());

        bookmarks.load();

        assertEquals(Map.of(
                "first", "bookmark",
                "2", "bookmarks"
        ), bookmarks.getAll());
    }

    @Test
    void canCreateBookmarksManagerFromExistingFileThenAddAndRemoveEntries() throws IOException {
        var tmpDir = Files.createTempDirectory("BookmarksManager");
        var file = tmpDir.resolve("bm2");
        Files.writeString(file, "\nfirst bookmark\n2 bookmarks", StandardOpenOption.CREATE_NEW);
        var bookmarks = new BookmarksManager(file, new TerminalPrinter());

        bookmarks.load();

        var added = bookmarks.add("3", "new");
        var removed = bookmarks.remove("2");

        // changes that should be ignored
        var added2 = bookmarks.add("3", "wrong");
        var removed2 = bookmarks.remove("foo");

        assertEquals(Map.of(
                "first", "bookmark",
                "3", "new"
        ), bookmarks.getAll());

        assertTrue(added);
        assertTrue(removed);
        assertFalse(added2);
        assertFalse(removed2);
    }
}
