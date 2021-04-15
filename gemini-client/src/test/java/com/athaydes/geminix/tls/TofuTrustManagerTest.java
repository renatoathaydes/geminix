package com.athaydes.geminix.tls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.athaydes.geminix.tls.CertificateTestHelper.loadCertificates;
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
        testCertificateHosts(CertificateTestHelper.geminiCertPath, CertificateTestHelper.geminiHosts);
    }

    @Test
    void canFindCertificateHosts2() throws Exception {
        testCertificateHosts(CertificateTestHelper.topotunCertPath, CertificateTestHelper.topotunHosts);
    }

    @Test
    void canFindCertificateHosts3() throws Exception {
        testCertificateHosts(CertificateTestHelper.tildeTeamCertPath, CertificateTestHelper.tildeHosts);
    }

    private void testCertificateHosts(String path, Set<String> expectedHosts) throws Exception {
        var certificates = loadCertificates(path);
        assertEquals(1, certificates.size());
        assertEquals(expectedHosts, TofuTrustManager.collectCertificateNames(certificates.get(0)));
    }
}
