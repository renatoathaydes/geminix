package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsCertificateStorage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public interface ErrorHandler {

    <T> Optional<T> run(Action<T> action);

    interface Action<T> {
        T run() throws IOException, ResponseParseError, URISyntaxException, TlsCertificateStorage.StorageException;
    }

}
