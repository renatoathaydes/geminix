package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;

import java.util.Optional;

final class TerminalErrorHandler implements ErrorHandler {

    @Override
    public <T> Optional<T> run(Action<T> action) {
        try {
            return Optional.ofNullable(action.run());
        } catch (Exception exception) {
            error(exception.toString());
            return Optional.empty();
        }
    }

    static void error(String message) {
        System.err.println("ERROR: " + message);
    }

}
