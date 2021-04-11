package com.athaydes.geminix.tls;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class TofuTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException("Cannot authenticate TLS client");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        System.out.println("-------------------------");
        System.out.println("CHAIN: " + Arrays.toString(chain));
        System.out.println("AUTH_TYPE: " + authType);
        System.out.println("-------------------------");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
