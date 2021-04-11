package com.athaydes.geminix.protocol;

import java.io.IOException;
import java.util.Optional;

public interface ResponseErrorHandler {

    <T> Optional<T> run(Action<T> action);

    interface Action<T> {
        T run() throws IOException, ResponseParseError;
    }

}
