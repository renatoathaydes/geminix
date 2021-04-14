package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.tls.TlsManager;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class TerminalTlsManager extends TlsManager {

    private final Map<String, byte[]> certificatePublicKeyByHost = new HashMap<>();
    private final UserInteractionManager userInteractionManager;

    public TerminalTlsManager(UserInteractionManager userInteractionManager) {
        this.userInteractionManager = userInteractionManager;
    }

    @Override
    public void handleCertificate(X509Certificate certificate,
                                  CertificateValidity certificateValidity,
                                  HostInformation hostInformation) {
        var encodedKey = certificate.getPublicKey().getEncoded();
        var subject = certificate.getSubjectX500Principal().getName();

        var hostStatus = "";
        if (!hostInformation.hostMatchesCertificateNames()) {
            hostStatus = "WARNING: Certificate issued for '" + hostInformation.certificateSubjectNames() +
                    "', but expected host is '" + hostInformation.connectionHost() + "'";
        }

        var cachedPubKey = certificatePublicKeyByHost.get(subject);

        if (cachedPubKey != null
                && certificateValidity == CertificateValidity.VALID
                && hostStatus.isEmpty()) {
            if (Arrays.equals(encodedKey, cachedPubKey)) {
                return;
            } else {
                System.out.println("WARNING: TLS Certificate for this host is not the same as last seen!");
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

        userInteractionManager.promptUser("""
                Do you want to accept and store the host certificate?
                    (1) Yes
                    (2) No
                    (3) Show Certificate""", (answer) -> {
            var option = answer.toLowerCase(Locale.ROOT).trim();

            switch (option) {
                case "1" -> {
                    certificatePublicKeyByHost.put(hostInformation.connectionHost(), encodedKey);
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
