package com.athaydes.geminix.tls;

import javax.net.ssl.X509TrustManager;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Trust-On-First-Use Certificate Trust Manager.
 */
final class TofuTrustManager implements X509TrustManager {

    private static final TofuTrustManager INSTANCE = new TofuTrustManager();

    public static TofuTrustManager getInstance() {
        return INSTANCE;
    }

    private TlsManager tlsManager;

    private TofuTrustManager() {
    }

    public void setTlsManager(TlsManager tlsManager) {
        this.tlsManager = tlsManager;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        throw new UnsupportedOperationException("Cannot authenticate TLS client");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        var cm = this.tlsManager;

        if (cm == null) {
            throw new IllegalStateException("CertificateManager has not been set on TofuTrustManager");
        }
        if (chain.length == 0) {
            throw new CertificateException("No certificate presented");
        }

        var certificateValidity = TlsManager.CertificateValidity.VALID;

        try {
            chain[0].checkValidity();
        } catch (CertificateExpiredException ignore) {
            certificateValidity = TlsManager.CertificateValidity.EXPIRED;
        } catch (CertificateNotYetValidException ignore) {
            certificateValidity = TlsManager.CertificateValidity.NOT_YET_VALID;
        }

        var certificateNames = collectCertificateNames(chain[0]);
        var hostInformation = new TlsManager.HostInformation(cm.getState().currentHost(), certificateNames);

        cm.handleCertificate(chain[0], certificateValidity, hostInformation);
    }

    static Set<String> collectCertificateNames(X509Certificate certificate) throws CertificateParsingException {
        var certificateName = certificate.getSubjectX500Principal().getName();
        var certificateCN = parseCertificateName(certificateName).get("CN");
        var alternativeNames = getAlternativeNames(certificate);
        return Stream.concat(
                Stream.of(certificateCN),
                alternativeNames.stream()
        ).collect(toSet());
    }

    private static List<String> getAlternativeNames(X509Certificate certificate) throws CertificateParsingException {
        var alternativeNames = new ArrayList<String>(4);
        var altNamesExtension = certificate.getSubjectAlternativeNames();
        if (altNamesExtension != null) {
            for (List<?> altName : altNamesExtension) {
                var name = altName.get(1);
                // the name entry may be a String or a DER-encoded byte-array
                if (name instanceof String nameStr) {
                    alternativeNames.add(nameStr);
                }
            }
        }
        return alternativeNames;
    }

    static Map<String, String> parseCertificateName(String value) {
        return Stream.of(value.split("\\s*,\\s*", 12))
                .map(part -> part.split("\\s*=\\s*", 2))
                .filter(entry -> entry.length == 2 && (!entry[0].isEmpty() || !entry[1].isEmpty()))
                .collect(toMap(entry -> entry[0], entry -> entry[1]));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
