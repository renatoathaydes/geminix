package com.athaydes.geminix.terminal.tls;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.tls.TlsCertificateStorage;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CachedTlsCertificateStorage implements TlsCertificateStorage {

    private final TlsCertificateStorage delegate;
    private final Map<String, X509Certificate> certificatePublicKeyByHost = new HashMap<>();

    public CachedTlsCertificateStorage(TlsCertificateStorage delegate,
                                       ErrorHandler errorHandler) {
        this.delegate = delegate;
        errorHandler.run(() -> {
            delegate.loadAll().forEach(certificatePublicKeyByHost::put);
            return null;
        });
    }

    @Override
    public Map<String, X509Certificate> loadAll() {
        return Collections.unmodifiableMap(certificatePublicKeyByHost);
    }

    @Override
    public Optional<X509Certificate> load(String host) {
        return Optional.ofNullable(certificatePublicKeyByHost.get(host));
    }

    @Override
    public void store(String host, X509Certificate certificate) throws StorageException {
        certificatePublicKeyByHost.put(host, certificate);
        delegate.store(host, certificate);
    }

    @Override
    public boolean remove(String host) throws StorageException {
        certificatePublicKeyByHost.remove(host);
        return delegate.remove(host);
    }

    @Override
    public void clean() throws StorageException {
        certificatePublicKeyByHost.clear();
        delegate.clean();
    }
}
