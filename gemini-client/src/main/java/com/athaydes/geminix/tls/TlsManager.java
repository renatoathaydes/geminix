package com.athaydes.geminix.tls;

import java.security.cert.X509Certificate;

public abstract class TlsManager {

    private State state;

     State getState() {
        return state;
    }

     void setState(State state) {
        this.state = state;
    }

    public abstract void handleCertificate(X509Certificate certificate, String certificateStatus, String hostStatus);

    static final record State(String currentHost) {
    }
}
