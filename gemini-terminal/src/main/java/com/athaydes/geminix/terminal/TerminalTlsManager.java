package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.terminal.tls.CachedTlsCertificateStorage;
import com.athaydes.geminix.tls.TlsManager;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Locale;

final class TerminalTlsManager extends TlsManager {

    private final UserInteractionManager userInteractionManager;
    private final CachedTlsCertificateStorage tlsCertificateStorage;

    public TerminalTlsManager(UserInteractionManager userInteractionManager,
                              CachedTlsCertificateStorage tlsCertificateStorage) {
        this.userInteractionManager = userInteractionManager;
        this.tlsCertificateStorage = tlsCertificateStorage;
    }

    @Override
    public void handleCertificate(X509Certificate certificate,
                                  CertificateValidity certificateValidity,
                                  HostInformation hostInformation) {
        var encodedKey = certificate.getPublicKey().getEncoded();
        var connectionHost = hostInformation.connectionHost();

        var hostStatus = "";
        if (!hostInformation.hostMatchesCertificateNames()) {
            hostStatus = "WARNING: Host " + connectionHost +
                    " presented a certificate issued for:\n" +
                    "  " + hostInformation.certificateSubjectNames();
        }

        var cachedPubKey = tlsCertificateStorage.load(connectionHost)
                .map(cert -> cert.getPublicKey().getEncoded())
                .orElse(null);

        if (cachedPubKey != null
                && certificateValidity == CertificateValidity.VALID
                && hostStatus.isEmpty()) {
            if (Arrays.equals(encodedKey, cachedPubKey)) {
                return;
            } else {
                System.out.println("WARNING: TLS Certificate for this host has been changed!");
            }
        }

        if (cachedPubKey == null) {
            System.out.println("INFO: First time accessing this host.");
        }
        if (!hostStatus.isEmpty()) {
            System.out.println(hostStatus);
        }
        if (certificateValidity != CertificateValidity.VALID) {
            System.out.println("WARNING: Certificate expiration status is " + certificateValidity);
        }

        System.out.println("Do you want to accept the certificate for host '" + connectionHost + "'?");

        userInteractionManager.promptUser("""
                    (1) Yes
                    (2) No
                    (3) Show Certificate""", (answer) -> {
            var option = answer.toLowerCase(Locale.ROOT).trim();

            switch (option) {
                case "1" -> {
                    userInteractionManager.getResponseErrorHandler().run(() -> {
                        tlsCertificateStorage.store(connectionHost, certificate);
                        return null;
                    });
                    return true;
                }
                case "2" -> throw new RuntimeException("Server Certificate was not accepted");
                case "3" -> {
                    showCertificate(certificate);
                    return false;
                }
                default -> {
                    System.out.println("ERROR: Please enter a valid option.");
                    return false;
                }
            }
        });
    }

    private static void showCertificate(X509Certificate certificate) {
        System.out.println("-------------------------");
        System.out.println("Certificate: " + certificate);
        System.out.println("-------------------------");
    }
}
