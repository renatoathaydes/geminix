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
    private final TerminalPrinter printer;

    public TerminalTlsManager(TerminalUserInteractionManager userInteractionManager,
                              CachedTlsCertificateStorage tlsCertificateStorage,
                              TerminalPrinter printer) {
        this.userInteractionManager = userInteractionManager;
        this.tlsCertificateStorage = tlsCertificateStorage;
        this.printer = printer;
    }

    @Override
    public void handleCertificate(X509Certificate certificate,
                                  CertificateValidity certificateValidity,
                                  HostInformation hostInformation) {
        var encodedKey = certificate.getPublicKey().getEncoded();
        var connectionHost = hostInformation.connectionHost();

        var hostStatus = "";
        if (!hostInformation.hostMatchesCertificateNames()) {
            hostStatus = "Host " + connectionHost +
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
                printer.warn("TLS Certificate for this host has been changed!");
            }
        }

        if (cachedPubKey == null) {
            printer.info("First time accessing this host.");
        }
        if (!hostStatus.isEmpty()) {
            printer.warn(hostStatus);
        }
        if (certificateValidity != CertificateValidity.VALID) {
            printer.warn("Certificate expiration status is " + certificateValidity);
        }

        userInteractionManager.promptUser("""
                Do you want to accept the certificate for host '""" + connectionHost + "'?\n" + """
                (1) Yes
                (2) No
                (3) Show Certificate""", (answer) -> {
            var option = answer.toLowerCase(Locale.ROOT).trim();

            switch (option) {
                case "1" -> {
                    userInteractionManager.getErrorHandler().run(() -> {
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
                    printer.error("Please enter a valid option.");
                    return false;
                }
            }
        });
    }

    private void showCertificate(X509Certificate certificate) {
        printer.info("-------------------------");
        printer.info("Certificate: " + certificate);
        printer.info("-------------------------");
    }
}
