package com.athaydes.geminix.tls;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FileTlsCertificateStorageTest {

    @Test
    void nonExistingFileReturnsEmptyValues() throws Exception {
        var dir = Files.createTempDirectory("file-tls-dir");
        var file = dir.resolve("file-tls.tmp");
        var storage = new FileTlsCertificateStorage(file);

        assertEquals(Map.of(), storage.loadAll());
        assertFalse(storage.load("foo").isPresent());

        assertFalse(file.toFile().exists());

        storage.clean();

        assertEquals(Map.of(), storage.loadAll());
        assertFalse(file.toFile().exists());
    }

    @Test
    void brokenFileThrowsExceptionOnLoad() throws Exception {
        var file = Files.createTempFile("file-tls", ".certs");
        Files.writeString(file, "host blahblahbalbhab");
        var storage = new FileTlsCertificateStorage(file);
        var exception = assertThrows(TlsCertificateStorage.StorageException.class,
                () -> storage.load("host"));
        assertEquals("Unable to decode certificate", exception.getMessage());
    }

    @Test
    void brokenFileThrowsExceptionOnLoadAll() throws Exception {
        var file = Files.createTempFile("file-tls", ".certs");
        Files.writeString(file, "host blahblahbalbhab");
        var storage = new FileTlsCertificateStorage(file);
        var exception = assertThrows(TlsCertificateStorage.StorageException.class,
                storage::loadAll);
        assertEquals("Unable to decode certificate", exception.getMessage());
    }

    @Test
    void canStoreBeforeCreationOfFile() throws Exception {
        var dir = Files.createTempDirectory("file-tls-dir");
        var file = dir.resolve("file-tls.tmp");
        var storage = new FileTlsCertificateStorage(file);

        var geminiCert = CertificateTestHelper.loadCertificates(CertificateTestHelper.geminiCertPath).get(0);

        storage.store("gemini", geminiCert);

        assertEquals(Set.of("gemini"), storage.loadAll().keySet());
    }

    @Test
    void canStoreAndLoadAndCleanCerts() throws Exception {
        var file = Files.createTempFile("file-tls", ".certs");
        var storage = new FileTlsCertificateStorage(file);

        var geminiCert = CertificateTestHelper.loadCertificates(CertificateTestHelper.geminiCertPath).get(0);

        storage.store("gemini", geminiCert);

        assertFalse(storage.load("foo").isPresent());
        assertTrue(storage.load("gemini").isPresent());
        assertArrayEquals(geminiCert.getEncoded(), storage.load("gemini").get().getEncoded());
        assertEquals(Set.of("gemini"), storage.loadAll().keySet());

        var topotunCert = CertificateTestHelper.loadCertificates(CertificateTestHelper.topotunCertPath).get(0);

        storage.store("topotun", topotunCert);

        assertFalse(storage.load("foo").isPresent());
        assertTrue(storage.load("gemini").isPresent());
        assertTrue(storage.load("topotun").isPresent());
        assertArrayEquals(geminiCert.getEncoded(), storage.load("gemini").get().getEncoded());
        assertArrayEquals(topotunCert.getEncoded(), storage.load("topotun").get().getEncoded());
        assertEquals(Set.of("gemini", "topotun"), storage.loadAll().keySet());

        var tildeCert = CertificateTestHelper.loadCertificates(CertificateTestHelper.tildeTeamCertPath).get(0);

        storage.store("tilde", tildeCert);

        assertFalse(storage.load("foo").isPresent());
        assertTrue(storage.load("gemini").isPresent());
        assertTrue(storage.load("topotun").isPresent());
        assertTrue(storage.load("tilde").isPresent());
        assertArrayEquals(geminiCert.getEncoded(), storage.load("gemini").get().getEncoded());
        assertArrayEquals(topotunCert.getEncoded(), storage.load("topotun").get().getEncoded());
        assertArrayEquals(tildeCert.getEncoded(), storage.load("tilde").get().getEncoded());
        assertEquals(Set.of("gemini", "topotun", "tilde"), storage.loadAll().keySet());

        // verify that the certificates do not get mixed up or broken on load
        assertEquals(CertificateTestHelper.geminiHosts,
                TofuTrustManager.collectCertificateNames(storage.load("gemini").get()));
        assertEquals(CertificateTestHelper.topotunHosts,
                TofuTrustManager.collectCertificateNames(storage.load("topotun").get()));
        assertEquals(CertificateTestHelper.tildeHosts,
                TofuTrustManager.collectCertificateNames(storage.load("tilde").get()));

        assertTrue(storage.remove("topotun"));

        assertFalse(storage.load("foo").isPresent());
        assertFalse(storage.load("topotun").isPresent());
        assertTrue(storage.load("gemini").isPresent());
        assertTrue(storage.load("tilde").isPresent());

        assertFalse(storage.remove("topotun"));

        storage.clean();

        assertEquals(Map.of(), storage.loadAll());
    }
}
