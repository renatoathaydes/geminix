package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;

public final class Geminix {

    public static void main(String[] args) {
        System.out.println("== Geminix ==");
        var uim = CommandLineUserInteractionManager.INSTANCE;
        var client = new Client(uim);

        uim.promptUser("Enter a URL or 'quit' to exit:", (userAnswer) -> {
            var answer = userAnswer.trim();
            var done = answer.equals("quit");
            if (!done && !answer.isEmpty()) {
                uim.getResponseErrorHandler().run(() -> {
                    client.sendRequest(answer);
                    return null;
                });
            }
            return done;
        });
    }

}
