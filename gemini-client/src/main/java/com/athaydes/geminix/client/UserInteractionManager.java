package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsManager;

import java.io.IOException;
import java.net.URI;
import java.util.function.Predicate;

public interface UserInteractionManager {

    void beforeRequest(URI target);

    void promptUser(String message, Predicate<String> acceptResponse);

    void showResponse(Response response) throws IOException;

    TlsManager getTlsManager();

    ErrorHandler getErrorHandler();

}
