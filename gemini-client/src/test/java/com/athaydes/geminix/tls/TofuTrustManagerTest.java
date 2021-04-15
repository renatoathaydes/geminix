package com.athaydes.geminix.tls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TofuTrustManagerTest {

    record ParseCertNameExample(String value, Map<String, String> expectedResult) {
    }

    static Stream<ParseCertNameExample> canParseCertName() {
        return Stream.of(
                new ParseCertNameExample("", Map.of()),
                new ParseCertNameExample("CN=Renato", Map.of("CN", "Renato")),
                new ParseCertNameExample("CN=Joe, OU=FOO", Map.of("CN", "Joe", "OU", "FOO")),
                new ParseCertNameExample("CN=Mary, OU =  G , L=Sydney,C=AUS",
                        Map.of("CN", "Mary", "OU", "G", "L", "Sydney", "C", "AUS"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void canParseCertName(ParseCertNameExample example) {
        assertEquals(example.expectedResult(), TofuTrustManager.parseCertificateName(example.value()));
    }

    @Test
    void canFindCertificateHosts1() throws Exception {
        testCertificateHosts("/gemini.cert", Set.of("geminispace.info"));
    }

    @Test
    void canFindCertificateHosts2() throws Exception {
        testCertificateHosts("/topotun.hldns.ru.cert", Set.of("topotun.hldns.ru"));
    }

    @Test
    void canFindCertificateHosts3() throws Exception {
        testCertificateHosts("/tilde.team.cert",
                Set.of("tilde.team", "tildeverse.org", "fuckup.club", "nand.sh", "ttm.sh",
                        "tild3.org", "tilde.chat", "tilde.life", "tilde.site", "tilde.wiki",
                        "tildeteam.net", "tildeteam.org", "tildeverse.net"));
    }

    private void testCertificateHosts(String path, Set<String> expectedHosts) throws Exception {
        try (var is = getClass().getResourceAsStream(path)) {
            var certificates = loadCert(is);
            assertEquals(1, certificates.size());
            assertEquals(expectedHosts, TofuTrustManager.collectCertificateNames(certificates.get(0)));
        }
    }

    private List<X509Certificate> loadCert(InputStream is) throws CertificateException, IOException {
        var bis = new BufferedInputStream(is);
        var cf = CertificateFactory.getInstance("X.509");

        var result = new ArrayList<X509Certificate>();
        while (bis.available() > 0) {
            var cert = (X509Certificate) cf.generateCertificate(bis);
            result.add(cert);
        }

        return result;
    }
}
