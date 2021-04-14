package com.athaydes.geminix.client;

import com.athaydes.geminix.tls.TlsManager;

import java.net.URI;
import java.util.function.Consumer;

public interface UserInteractionManager extends TlsManager, ResponseErrorHandler {

    void beforeRequest(URI target);

    void promptUser(String message, Consumer<String> response);

    void showResponse(Response response);

}
