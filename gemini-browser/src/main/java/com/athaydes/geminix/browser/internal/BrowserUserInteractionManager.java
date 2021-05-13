package com.athaydes.geminix.browser.internal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.tls.TlsManager;

import java.io.IOException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BrowserUserInteractionManager implements UserInteractionManager {
    private final Consumer<Response> responseConsumer;

    public BrowserUserInteractionManager(Consumer<Response> responseConsumer) {
        this.responseConsumer = responseConsumer;
    }

    @Override
    public void beforeRequest(URI target) {
    }

    @Override
    public void promptUser(String message, Predicate<String> acceptResponse) {
        throw new UnsupportedOperationException("promptUser");
    }

    @Override
    public void showResponse(Response response) throws IOException {
        responseConsumer.accept(response);
    }

    @Override
    public TlsManager getTlsManager() {
        // TODO implement UI
        return new TlsManager() {
            @Override
            public void handleCertificate(X509Certificate certificate, CertificateValidity certificateValidity, HostInformation hostInformation) {

            }
        };
    }

    @Override
    public ErrorHandler getErrorHandler() {
        // TODO implement UI
        return new ErrorHandler() {
            @Override
            public <T> Optional<T> run(Action<T> action) {
                try {
                    return Optional.ofNullable(action.run());
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            }
        };
    }
}
