package com.athaydes.geminix.tls;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CertificateTestHelper {
    static String tildeTeamCertPath = "/tilde.team.cert";
    static String topotunCertPath = "/topotun.hldns.ru.cert";
    static String geminiCertPath = "/gemini.cert";

    static Set<String> geminiHosts = Set.of("geminispace.info");

    static Set<String> topotunHosts = Set.of("topotun.hldns.ru");

    static Set<String> tildeHosts = Set.of("tilde.team", "tildeverse.org", "fuckup.club", "nand.sh", "ttm.sh",
            "tild3.org", "tilde.chat", "tilde.life", "tilde.site", "tilde.wiki",
            "tildeteam.net", "tildeteam.org", "tildeverse.net");

    static List<X509Certificate> loadCertificates(String path) {
        try (var is = CertificateTestHelper.class.getResourceAsStream(path)) {
            return loadCertificates(is);
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static List<X509Certificate> loadCertificates(InputStream is) throws CertificateException, IOException {
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
