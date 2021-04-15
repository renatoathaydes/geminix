package com.athaydes.geminix.tls;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

public interface TlsCertificateStorage {

    Map<String, X509Certificate> loadAll() throws StorageException;

    Optional<X509Certificate> load(String host) throws StorageException;

    void store(String host, X509Certificate certificate) throws StorageException;

    boolean remove(String host) throws StorageException;

    void clean() throws StorageException;

    class StorageException extends Exception {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
