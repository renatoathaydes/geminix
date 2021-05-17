package com.athaydes.geminix.browser.internal;

import java.net.URI;
import java.util.LinkedList;
import java.util.Optional;

public final class BrowsingHistory {
    private static final int MAX_ENTRIES = 100;

    private final LinkedList<URI> entries = new LinkedList<>();
    private int index = 0;

    void add(URI uri) {
        if (entries.size() == MAX_ENTRIES) {
            entries.removeFirst();
        }
        entries.addLast(uri);
        index = entries.size();
    }

    void clear() {
        entries.clear();
        index = 0;
    }

    Optional<URI> back() {
        if (index == -1) {
            return Optional.empty();
        }
        if (index == entries.size()) {
            // at edge, must move away from it
            index--;
        }
        index--;
        if (index >= 0) {
            return Optional.of(entries.get(index));
        }
        return Optional.empty();
    }

    Optional<URI> forth() {
        if (index == entries.size()) {
            return Optional.empty();
        }
        if (index < 0) {
            // at edge, must move away from it
            index++;
        }
        index++;
        if (index < entries.size()) {
            return Optional.of(entries.get(index));
        }
        return Optional.empty();
    }

}
