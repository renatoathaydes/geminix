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
    public void handleCertificate(X509Certificate certificate, String certificateStatus, String hostStatus) {
        var encodedKey = certificate.getPublicKey().getEncoded();
        var subject = certificate.getSubjectX500Principal().getName();
        var cachedPubKey = certificatePublicKeyByHost.get(subject);

        if (cachedPubKey != null && certificateStatus.isEmpty() && hostStatus.isEmpty()) {
            if (Arrays.equals(encodedKey, cachedPubKey)) {
                System.out.println("TLS Certificate approved");
                return;
            } else {
                System.out.println("ERROR: TLS Certificate for this host has been modified!");
            }
        }

        System.out.println("-------------------------");
        System.out.println("NEW Certificate: " + certificate);
        System.out.println("Status: " + (certificateStatus.isEmpty() ? "OK" : certificateStatus));
        System.out.println("Host: " + (hostStatus.isEmpty() ? "OK" : hostStatus));
        System.out.println("-------------------------");

        userInteractionManager.promptUser("Do you want to accept the above certificate? [y/n]", (answer) -> {
            if (answer.toLowerCase(Locale.ROOT).trim().equals("y")) {
                certificatePublicKeyByHost.put(subject, encodedKey);
            } else {
                System.out.println("Aborting request");
                throw new RuntimeException("Server Certificate was not accepted");
            }
        });
    }
}
