package com.athaydes.geminix.tls;

import java.security.cert.X509Certificate;

public interface TlsManager {

    State getState();

    void setState(State state);

    void handleCertificate(X509Certificate certificate, String certificateStatus, String hostStatus);

    final record State(String currentHost) {
    }
}
