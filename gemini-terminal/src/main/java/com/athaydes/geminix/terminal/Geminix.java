package com.athaydes.geminix.terminal;

public final class Geminix {


    public static void main(String[] args) {
        System.out.println("================== Geminix ==================");
        System.out.println("  https://github.com/renatoathaydes/geminix  ");
        System.out.println("=============================================");

        var uim = Objects.uim;
        var commandHandler = Objects.commandHandler;
        var client = Objects.client;

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
