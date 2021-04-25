package com.athaydes.geminix.terminal;

import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.text.GemTextLine;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;
import java.util.stream.IntStream;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class CompleterFactory {
    private final CachedTlsCertificateStorage certificateStorage;
    private final BookmarksManager bookmarks;
    private final List<GemTextLine.Link> links;

    public CompleterFactory(CachedTlsCertificateStorage certificateStorage,
                            BookmarksManager bookmarks,
                            List<GemTextLine.Link> links) {
        this.certificateStorage = certificateStorage;
        this.bookmarks = bookmarks;
        this.links= links;
    }

    Completer create() {
        var hostsCompleter = node(new StringsCompleter(() ->
                certificateStorage.loadAll().keySet().stream().toList()));

        var bookmarkCompleter = node(new StringsCompleter(() ->
                bookmarks.getAll().keySet().stream().sorted().toList()));

        var linksCompleter = node(new StringsCompleter(() ->
                IntStream.range(0, links.size()).mapToObj(Integer::toString).toList()));

        return new Completers.TreeCompleter(
                node(".help",
                        node("help", "quit", "colors", "prompt", "bookmark", "bm", "link", "certs", "width")),
                node(".width"),
                node(".q"),
                node(".quit"),
                node(".link", linksCompleter),
                node(".colors",
                        node("on", "off"),
                        node("info", "warn", "error", "prompt", "h1", "h2", "h3", "link", "list", "quote",
                                node("black", "red", "green", "yellow", "blue", "magenta", "cyan", "white", "default"))),
                node(".prompt"),
                node(".bookmark",
                        node("add", "show"),
                        node("rm", bookmarkCompleter),
                        node("go", bookmarkCompleter),
                        bookmarkCompleter),
                node(".b",
                        node("add", "show"),
                        node("rm", bookmarkCompleter),
                        node("go", bookmarkCompleter),
                        bookmarkCompleter),
                node(".certs",
                        node("server",
                                node("show", hostsCompleter),
                                node("rm", hostsCompleter),
                                "clear"))
        );
    }
}
