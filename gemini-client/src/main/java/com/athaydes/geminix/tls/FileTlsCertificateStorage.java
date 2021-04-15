package com.athaydes.geminix.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class FileTlsCertificateStorage implements TlsCertificateStorage {

    private static record Entry(String host, X509Certificate certificate) {
    }

    private final Path file;

    public FileTlsCertificateStorage(Path file) {
        this.file = file;
    }

    @Override
    public Map<String, X509Certificate> loadAll() throws StorageException {
        try {
            return parseLines(Files.lines(file, StandardCharsets.UTF_8))
                    .collect(Collectors.toMap(Entry::host, Entry::certificate));
        } catch (NoSuchFileException e) {
            return Map.of();
        } catch (IOException e) {
            throw new StorageException("Unable to load certificates from " + file, e);
        } catch (IllegalStateException e) {
            throw new StorageException(e.getMessage(), e.getCause());
        }
    }

    public Optional<X509Certificate> load(String host) throws StorageException {
        try {
            return find(host, Files.lines(file, StandardCharsets.UTF_8));
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new StorageException("Unable to load certificates from " + file, e);
        }
    }

    @Override
    public boolean remove(String host) throws StorageException {
        String lines;
        var found = new AtomicBoolean(false);
        try {
            // collect all lines that are NOT the entry being removed
            lines = Files.lines(file).filter(line -> {
                var i = line.indexOf(' ');
                if (i < 0) return false;
                var entryHost = line.substring(0, i);
                var isEntryToRemove = entryHost.equals(host);
                if (isEntryToRemove) {
                    found.set(true);
                }
                return !isEntryToRemove;
            }).collect(joining("\n"));
        } catch (IOException e) {
            throw new StorageException("Unable to read file " + file, e);
        }
        try {
            // overwrite the file
            Files.writeString(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Unable to restore non-removed entries to " + file +
                    "\nPlease try restoring the file manually with these contents:\n" +
                    "\n------------------------------\n" +
                    lines +
                    "\n------------------------------\n", e);
        }
        return found.get();
    }

    @Override
    public void clean() throws StorageException {
        try {
            Files.delete(file);
        } catch (NoSuchFileException e) {
            // ignore
        } catch (IOException e) {
            throw new StorageException("Unable to delete file " + file, e);
        }
    }

    public void store(String host, X509Certificate certificate) throws StorageException {
        try {
            Files.writeString(file, createLine(host, certificate), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (CertificateEncodingException | IOException e) {
            throw new StorageException("Unable to encode certificate", e);
        }
    }

    private Optional<X509Certificate> find(String host, Stream<String> lines) throws StorageException {
        try {
            return parseLines(lines)
                    .filter(entry -> host.equals(entry.host()))
                    .map(Entry::certificate)
                    .findAny();
        } catch (IllegalStateException e) {
            throw new StorageException(e.getMessage(), e.getCause());
        }
    }

    private Stream<Entry> parseLines(Stream<String> lines) {
        return lines.map(line -> line.trim().isEmpty() ? new String[0] : line.split(" ", 2))
                .filter(parts -> parts.length == 2)
                .map(parts -> new Entry(parts[0], decodeCert(parts[1])));
    }

    private String createLine(String host, X509Certificate certificate) throws CertificateEncodingException {
        return "\n" + host + " " + Base64.getEncoder().encodeToString(certificate.getEncoded());
    }

    private X509Certificate decodeCert(String encoded) {
        try {
            var cf = CertificateFactory.getInstance("X.509");
            var is = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
            return (X509Certificate) cf.generateCertificate(is);
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to decode certificate", e);
        }
    }
}
