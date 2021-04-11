package com.athaydes.geminix.tls;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public interface TlsSocketFactory {
    SSLSocket create(String host, int port) throws IOException;

    static TlsSocketFactory defaultFactory() {
        return DefaultTlsSocketFactory.INSTANCE;
    }

    final class DefaultTlsSocketFactory implements TlsSocketFactory {

        private static final String[] PROTOCOLS = new String[]{"TLSv1.2", "TLSv1.3"};

        private static final TlsSocketFactory INSTANCE = new DefaultTlsSocketFactory();

        private DefaultTlsSocketFactory() {
        }

        public SSLSocket create(String host, int port) throws IOException {
            SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault()
                    .createSocket(host, port);
            socket.setEnabledProtocols(PROTOCOLS);
            return socket;
        }
    }
}
