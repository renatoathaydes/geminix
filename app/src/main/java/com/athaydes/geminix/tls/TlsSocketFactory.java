package com.athaydes.geminix.tls;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public interface TlsSocketFactory {
    SSLSocket create(String host, int port) throws IOException;

    static TlsSocketFactory defaultFactory() {
        return DefaultTlsSocketFactory.INSTANCE;
    }

    final class DefaultTlsSocketFactory implements TlsSocketFactory {

        private static final String[] PROTOCOLS = new String[]{"TLSv1.2", "TLSv1.3"};

        private static final TlsSocketFactory INSTANCE = new DefaultTlsSocketFactory();

        private final SSLContext sslContext;

        private DefaultTlsSocketFactory() {
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("TLS");
                var myTrustManager = new TofuTrustManager();
                sslContext.init(null, new TrustManager[]{myTrustManager}, null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IllegalStateException(e);
            }
            this.sslContext = sslContext;
        }

        public SSLSocket create(String host, int port) throws IOException {
            var socket = (SSLSocket) sslContext.getSocketFactory()
                    .createSocket(host, port);
            socket.setEnabledProtocols(PROTOCOLS);
            return socket;
        }
    }
}
