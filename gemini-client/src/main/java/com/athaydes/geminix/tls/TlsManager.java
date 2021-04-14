package com.athaydes.geminix.tls;

import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Set;

public abstract class TlsManager {

    private State state;

    State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    public abstract void handleCertificate(X509Certificate certificate,
                                           CertificateValidity certificateValidity,
                                           HostInformation hostInformation);

    static final record State(String currentHost) {
    }

    public enum CertificateValidity {
        VALID, EXPIRED, NOT_YET_VALID
    }

    public static record HostInformation(
            String connectionHost,
            Set<String> certificateSubjectNames
    ) {
        public boolean hostMatchesCertificateNames() {
            for (String certificateSubjectName : certificateSubjectNames) {
                if (certificateSubjectName.startsWith("*")) {
                    var suffix = certificateSubjectName.substring(1);
                    if (connectionHost.toLowerCase(Locale.ROOT)
                            .endsWith(suffix.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                } else if (connectionHost.equalsIgnoreCase(certificateSubjectName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
