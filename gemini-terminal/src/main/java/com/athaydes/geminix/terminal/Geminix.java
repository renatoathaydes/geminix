package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;

public final class Geminix {

    public static void main(String[] args) {
        System.out.println("================== Geminix ==================");
        System.out.println("  https://github.com/renatoathaydes/geminix  ");
        System.out.println("=============================================");

        var uim = CommandLineUserInteractionManager.INSTANCE;
        var client = new Client(uim);
        var commandHandler = uim.getCommandHandler();

        uim.promptUser("Enter a URL or command (enter '.help' for help, '.quit' to exit):", (userAnswer) -> {
            var answer = userAnswer.trim();
            var done = false;
            if (answer.startsWith(".")) {
                done = commandHandler.handle(answer.substring(1));
            } else if (!answer.isEmpty()) {
                client.sendRequest(answer);
            }
            if (done) {
                uim.close();
            }
            return done;
        });
    }

}
