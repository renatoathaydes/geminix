package com.athaydes.geminix.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;

public interface UserInteractionManager {

    void promptUser(String message, Consumer<String> response);

    void showResponse(Response response);

    static UserInteractionManager simpleCommandLine() {
        return CommandLineUserInteractionManager.INSTANCE;
    }

    final class CommandLineUserInteractionManager implements UserInteractionManager {
        static final CommandLineUserInteractionManager INSTANCE = new CommandLineUserInteractionManager();

        private CommandLineUserInteractionManager() {
        }

        @Override
        public void promptUser(String message, Consumer<String> onResponse) {
            System.out.println("PROMPT: " + message);
            var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            var userResponse = scanner.nextLine();
            onResponse.accept(userResponse);
        }

        @Override
        public void showResponse(Response response) {
            System.out.println(response.statusCode().name());
        }
    }
}
