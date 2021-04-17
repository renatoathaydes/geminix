package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;

import java.util.Optional;

final class TerminalErrorHandler implements ErrorHandler {

    private final TerminalPrinter printer;

    TerminalErrorHandler(TerminalPrinter printer) {
        this.printer = printer;
    }

    @Override
    public <T> Optional<T> run(Action<T> action) {
        try {
            return Optional.ofNullable(action.run());
        } catch (Exception exception) {
            printer.error(exception.toString());
            return Optional.empty();
        }
    }

}
