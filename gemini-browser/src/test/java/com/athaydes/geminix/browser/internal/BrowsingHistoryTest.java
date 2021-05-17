package com.athaydes.geminix.browser.internal;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrowsingHistoryTest {

    @Test
    void cannotMoveAroundEmpty() {
        var history = new BrowsingHistory();

        assertTrue(history.forth().isEmpty(), "Cannot move forth when empty");
        assertTrue(history.back().isEmpty(), "Cannot move back when empty");

        history = new BrowsingHistory();

        assertTrue(history.back().isEmpty(), "Cannot move back when empty");
        assertTrue(history.forth().isEmpty(), "Cannot move forth when empty");
    }

    @Test
    void cannotMoveAroundSingleItem() {
        var history = new BrowsingHistory();
        history.add(URI.create("foo"));

        assertTrue(history.forth().isEmpty(), "Cannot move forth with one item");
        assertTrue(history.back().isEmpty(), "Cannot move back with one item");

        history = new BrowsingHistory();
        history.add(URI.create("foo"));

        assertTrue(history.back().isEmpty(), "Cannot move back with one item");
        assertTrue(history.forth().isEmpty(), "Cannot move forth with one item");
    }

    @Test
    void canKeepHistoryAndMoveAround() {
        var history = new BrowsingHistory();
        history.add(URI.create("abc"));
        history.add(URI.create("def"));

        assertTrue(history.forth().isEmpty(), "Cannot move forth before moving back");
        assertEquals(Optional.of(URI.create("abc")), history.back(), "Can move back when 2 entries exist");
        assertTrue(history.back().isEmpty(), "Cannot move back when reaching the first entry");
        assertEquals(Optional.of(URI.create("def")), history.forth(), "Can move forth after moving back");

        history = new BrowsingHistory();
        history.add(URI.create("abc"));
        history.add(URI.create("def"));

        assertEquals(Optional.of(URI.create("abc")), history.back(), "Can move back with two items");
        assertTrue(history.back().isEmpty(), "Cannot move back when reaching the first entry");
        assertEquals(Optional.of(URI.create("def")), history.forth(), "Can move forth after moving back");
        assertTrue(history.forth().isEmpty(), "Cannot move forth after reaching last entry");
        assertEquals(Optional.of(URI.create("abc")), history.back(), "Can move back again after moving forth");
    }
}
