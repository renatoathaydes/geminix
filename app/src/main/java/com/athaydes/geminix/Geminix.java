package com.athaydes.geminix;

import com.athaydes.geminix.protocol.Client;
import com.athaydes.geminix.protocol.UserInteractionManager;
import com.athaydes.geminix.util.UriHelper;

public final class Geminix {
    private static String userAnswer;

    public static void main(String[] args) {
        System.out.println("== Geminix ==");
        var uim = UserInteractionManager.simpleCommandLine();

        while (true) {
            uim.promptUser("Enter a URL or 'quit' to exit:", (answer) -> userAnswer = answer);

            if ("quit".equals(userAnswer)) {
                break;
            }

            var client = new Client(uim);

            uim.run(() -> {
                client.sendRequest(UriHelper.geminify(userAnswer));
                return null;
            });
        }
    }

}
