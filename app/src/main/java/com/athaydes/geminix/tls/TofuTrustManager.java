package com.athaydes.geminix.tls;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

final class TofuTrustManager implements X509TrustManager {

    private static final TofuTrustManager INSTANCE = new TofuTrustManager();

    public static TofuTrustManager getInstance() {
        return INSTANCE;
    }

    private TlsManager tlsManager;

    private TofuTrustManager() {
    }

    public void setTlsManager(TlsManager tlsManager) {
        this.tlsManager = tlsManager;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        throw new UnsupportedOperationException("Cannot authenticate TLS client");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        var cm = this.tlsManager;

        if (cm == null) {
            throw new IllegalStateException("CertificateManager has not been set on TofuTrustManager");
        }

        var certificateStatus = "";

        try {
            chain[0].checkValidity();
        } catch (CertificateExpiredException ignore) {
            certificateStatus = "Expired";
        } catch (CertificateNotYetValidException ignore) {
            certificateStatus = "NotYetValid";
        }

        var hostStatus = "";

        var certificateSubject = chain[0].getSubjectX500Principal().getName();

        if (!certificateSubject.equals(cm.getState().currentHost())) {
            hostStatus = "WARNING: Certificate issued for '" + certificateSubject +
                    "', but expected host is '" + cm.getState().currentHost() + "'";
        }

        cm.handleCertificate(chain[0], certificateStatus, hostStatus);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
