package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;

public final class Geminix {
    private static String userAnswer;

    public static void main(String[] args) {
        System.out.println("== Geminix ==");
        var uim = CommandLineUserInteractionManager.INSTANCE;

        while (true) {
            uim.promptUser("Enter a URL or 'quit' to exit:", (answer) -> userAnswer = answer);

            if ("quit".equals(userAnswer)) {
                break;
            }

            var client = new Client(uim);

            uim.getResponseErrorHandler().run(() -> {
                client.sendRequest(userAnswer);
                return null;
            });
        }
    }

}
