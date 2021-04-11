package com.athaydes.geminix;

import com.athaydes.geminix.protocol.Client;
import com.athaydes.geminix.protocol.ResponseErrorHandler;
import com.athaydes.geminix.protocol.ResponseParseError;
import com.athaydes.geminix.protocol.UserInteractionManager;
import com.athaydes.geminix.util.UriHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public final class Geminix {

    public static void main(String[] args) {
        System.out.println("Geminix");
        System.out.println("Enter a URL:");
        var scanner = new Scanner(System.in);
        var uri = scanner.nextLine();

        var client = new Client(UserInteractionManager.simpleCommandLine());

        client.sendRequest(UriHelper.geminify(uri), new ResponseErrorHandler() {
            @Override
            public <T> Optional<T> run(Action<T> action) {
                try {
                    return Optional.ofNullable(action.run());
                } catch (ResponseParseError | IOException exception) {
                    error(exception.toString());
                    return Optional.empty();
                }
            }
        });
    }

    static void error(String message) {
        System.err.println("ERROR: " + message);
        System.exit(1);
    }

}
