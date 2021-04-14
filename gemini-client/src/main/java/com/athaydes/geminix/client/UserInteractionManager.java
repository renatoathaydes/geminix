package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsManager;

import java.net.URI;
import java.util.function.Consumer;

public interface UserInteractionManager  {

    void beforeRequest(URI target);

    void promptUser(String message, Consumer<String> response);

    void showResponse(Response response);

    TlsManager getTlsManager();

    ErrorHandler getResponseErrorHandler();

}
