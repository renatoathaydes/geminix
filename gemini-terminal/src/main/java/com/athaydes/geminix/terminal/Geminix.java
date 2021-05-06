package com.athaydes.geminix.terminal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Geminix {


    public static void main(String[] args) {
        System.out.println("================== Geminix ==================");
        System.out.println("  https://github.com/renatoathaydes/geminix  ");
        System.out.println("=============================================");

        var uim = Objects.uim;

        System.out.println("Enter a URL or command (enter '.help' for help, '.quit' to exit, Tab for auto-completion).");

        processStartupFile(Files.INSTANCE.getStartup(), uim);

        uim.promptUser("", userAnswer -> commandLooop(userAnswer, uim));
    }

    private static void processStartupFile(Path startup, TerminalUserInteractionManager uim) {
        var startupFile = startup.toFile();
        if (startupFile.isFile()) {
            uim.getErrorHandler().run(() -> {
                java.nio.file.Files.readAllLines(startup, StandardCharsets.UTF_8).stream()
                        .filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .forEach(line -> commandLooop(line, uim));
                return null;
            });
        }
    }

    private static boolean commandLooop(String userAnswer, TerminalUserInteractionManager uim) {
        var commandHandler = Objects.commandHandler;
        var client = Objects.client;

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
    }

}
