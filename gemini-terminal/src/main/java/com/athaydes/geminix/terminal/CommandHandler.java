package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.tls.TlsCertificateStorage;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

final class CommandHandler {
    private static final String HELP = """
            Geminix is a Gemini Client.
                       
            You can use it to navigate the Geminspace, much like you can use a web browser to navigate the WWW.
                        
            To visit a URL, simply type it in. The scheme and port are optional.
            The following are example URIs you can try:
                        
            => gemini.circumlunar.space/
            => geminispace.info/
            => gemini://tobykurien.com/
                        
            Geminix supports the following commands:
                        
            * bookmark <args> - manages bookmarks.
            * b <args>        - alias to bookmark.
            * certs <args>    - manages TLS certificates.
            * colors <args>   - manages output colors.
            * help            - shows this help message.
            * help <cmd>      - show help for a given command.
            * h               - alias to help.
            * link [<idx>]    - display links or follow a link.
            * l               - alias to link.
            * prompt <p>      - sets the prompt.
            * quit            - quits Geminix.
            * q               - alias to quit.
            * width [<chars>] - set max text width.
                        
            To enter a command, prefix it with a '.'.
                        
            For example, to see help for the colors command, type:
                        
              .help colors
                        
            """;

    private static final String COLORS_HELP = """
            The 'colors' command accepts the following arguments:
                        
            * <item> <color>   - set the color to use for an item.
            * off              - turn off terminal colors.
            * on               - turn on terminal colors.
                        
            Available items are:
                        
            * info, warn, error (Log Levels).
            * h1, h2, h3, quote, list, link (GemText lines).
            * prompt (User Prompt).
                        
            Valid colors are:
                        
            * black
            * red
            * green
            * yellow
            * blue
            * magenta
            * cyan
            * white
            * default
            """;

    private static final String LINK_HELP = """
            The link command is used to display the URL of, and follow links from the current page.
                        
            Links are displayed with an index between square brackets (e.g. [1]). To follow a link, pass the link \
            index as an argument to this command.
                        
            To display the URL a link refers to, write 'url' after the index. For example:
                        
            > .link 3 url
                        
            When invoked without an argument, the link command will display all available links.
            """;

    private static final String HELP_HELP = """
            The help command can be called with zero or one argument.
                        
            Without an argument, it prints the Geminix main help page.
                        
            If given a command as an argument, it prints command-specific help.
            """;

    private static final String PROMPT_HELP = """
            The prompt command changes the prompt which is displayed when Geminix expects user input.
            """;

    private static final String CERTS_HELP = """
            The certs command can be used to manage TLS certificates.
                        
            It accepts the following arguments:
                        
            * server show [hosts] - shows the certificate for one or more hosts,
                                    or for all hosts if no host is given.
            * server hosts        - shows all hosts for which a TLS certificate has been cached.
            * server rm <host>    - remove the cached certificate for the given host.
            * server clear        - remove all cached certificates for all hosts.
            """;

    private static final String BOOKMARK_HELP = """
            The bookmark (bm) command is used to manage and use bookmarks.
                        
            It accepts the following arguments:
                        
            * add <name> <url> - create a bookmark.
            * rm <name>        - remove a bookmark.
            * show             - show all bookmarks.
            * go <name>        - send a request to the URL associated with the given bookmark.
            * <name>           - same as 'go <name>' as long as <name> is not a sub-command.
                        
            To open the URL associated with a bookmark, simply type '.bm <name>'.
                        
            For example:
                        
            ```
            # Add bookmark
            .bm add gmn gemini.circumlunar.space/
                        
            # Navigate to the bookmarked URL
            .bm gmn
            ```
            """;

    private static final String QUIT_HELP = """
            The quit command exits Geminix.
            """;

    private static final String WIDTH_HELP = """
            The width command shows or sets the maximum width, in characters, of each text line of content.
                        
            The minimum width allowed is 10, and the maximum is 10_000.
            """;

    private final TerminalPrinter printer;
    private final TlsCertificateStorage certificateStorage;
    private final ErrorHandler errorHandler;
    private final TerminalUserInteractionManager uim;
    private final BookmarksManager bookmarks;
    private final Client client;

    public CommandHandler(TlsCertificateStorage certificateStorage,
                          TerminalPrinter printer,
                          ErrorHandler errorHandler,
                          BookmarksManager bookmarks,
                          TerminalUserInteractionManager uim,
                          Client client) {
        this.certificateStorage = certificateStorage;
        this.printer = printer;
        this.errorHandler = errorHandler;
        this.bookmarks = bookmarks;
        this.uim = uim;
        this.client = client;
    }

    /**
     * Handles a user command.
     *
     * @param answer user input
     * @return true to exit CLI loop
     */
    public boolean handle(String answer) {
        if (!answer.isEmpty()) {
            var cmd = answer.split("\\s+");
            switch (cmd[0]) {
                case "help" -> handleHelp(cmd);
                case "colors" -> handleColors(cmd);
                case "width" -> handleWidth(cmd);
                case "prompt" -> handlePrompt(answer.substring("prompt".length()));
                case "bookmark", "b" -> handleBookmark(cmd);
                case "link", "l" -> handleLink(cmd);
                case "certs" -> handleCerts(cmd);
                default -> printer.error("Invalid command: " + answer);
                case "quit", "q" -> {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleHelp(String[] cmd) {
        switch (cmd.length) {
            case 1 -> printer.info(HELP);
            case 2 -> handleHelpFor(cmd[1]);
            default -> printer.error("help command takes 0 or 1 arguments.");
        }
    }

    private void handleColors(String[] cmd) {
        switch (cmd.length) {
            case 2 -> {
                switch (cmd[1]) {
                    case "off" -> printer.colors(false);
                    case "on" -> printer.colors(true);
                    default -> printer.error("unrecognized argument.");
                }
            }
            case 3 -> handleColors(cmd[1], cmd[2]);
            default -> printer.error("colors command takes 1 or 2 arguments.");
        }
    }

    private void handleColors(String level, String color) {
        Ansi.Color ansiColor;
        try {
            ansiColor = Ansi.Color.valueOf(color.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            printer.error("Invalid color: " + color + "! Valid colors are " + Arrays.toString(Ansi.Color.values()) + ".");
            return;
        }
        switch (level) {
            case "info" -> printer.setInfoColor(ansiColor);
            case "warn" -> printer.setWarnColor(ansiColor);
            case "error" -> printer.setErrorColor(ansiColor);
            case "h1" -> printer.setH1Color(ansiColor);
            case "h2" -> printer.setH2Color(ansiColor);
            case "h3" -> printer.setH3Color(ansiColor);
            case "link" -> printer.setLinkColor(ansiColor);
            case "list" -> printer.setListColor(ansiColor);
            case "quote" -> printer.setQuoteColor(ansiColor);
            case "prompt" -> printer.setPromptColor(ansiColor);
            default -> printer.error("Invalid log level: " + level);
        }
    }

    private void handleHelpFor(String cmd) {
        switch (cmd) {
            case "colors" -> printer.info(COLORS_HELP);
            case "help" -> printer.info(HELP_HELP);
            case "prompt" -> printer.info(PROMPT_HELP);
            case "bookmark" -> printer.info(BOOKMARK_HELP);
            case "certs" -> printer.info(CERTS_HELP);
            case "width" -> printer.info(WIDTH_HELP);
            case "link" -> printer.info(LINK_HELP);
            case "quit" -> printer.info(QUIT_HELP);
            default -> printer.error("Unknown command: " + cmd);
        }
    }

    private void handleWidth(String[] cmd) {
        if (cmd.length == 1) {
            printer.info("The maximum width is set to " + printer.getMaxTextWidth() + " characters.");
        } else if (cmd.length == 2) {
            int newWidth;
            try {
                newWidth = Integer.parseInt(cmd[1]);
            } catch (NumberFormatException e) {
                printer.error("Bad argument, expected an integer value.");
                return;
            }
            if (newWidth < 10 || newWidth > 10_000) {
                printer.error("Width is out of range 10-10_000");
            } else {
                printer.setMaxTextWidth(newWidth);
            }
        } else {
            printer.error("Missing argument for width command");
        }
    }

    private void handlePrompt(String text) {
        printer.setPrompt(text.trim() + " ");
    }

    private void handleLink(String[] cmd) {
        var links = uim.getLinks();
        if (cmd.length == 1) {
            printer.info("The current page is " + uim.getCurrentUrl() +
                    (links.isEmpty()
                            ? ". It has no links."
                            : " and it contains " + links.size() + " link" + (links.size() == 1 ? "" : "s") + ":\n"));
            for (int i = 0; i < links.size(); i++) {
                var link = links.get(i);
                printer.print(link, i);
            }
        } else if (cmd.length == 2 || cmd.length == 3) {
            if (uim.getCurrentUrl() == null) {
                printer.error("No URL visited yet, cannot follow any links.");
                return;
            }
            int linkIndex;
            try {
                linkIndex = Integer.parseInt(cmd[1]);
            } catch (NumberFormatException e) {
                printer.error("Bad argument, expected an integer value.");
                return;
            }
            if (linkIndex < links.size()) {
                uim.getErrorHandler().run(() -> {
                    var destination = client.getLinkDestination(uim.getCurrentUrl(), links.get(linkIndex));
                    if (cmd.length == 3) {
                        if (cmd[2].equalsIgnoreCase("url")) {
                            printer.print(links.get(linkIndex), linkIndex);
                            printer.print("  " + destination, printer.linkColor);
                        } else {
                            printer.error("Expected 'url' after link index.");
                        }
                    } else {
                        uim.getHistory().add(destination.toString());
                        client.sendRequest(destination);
                    }
                    return null;
                });

            } else {
                printer.error("Bad argument, integer value is out of range 0-" + (links.size() - 1) + ".");
            }
        } else {
            printer.error("Too many arguments for link command.");
        }
    }

    private void handleBookmark(String[] cmd) {
        if (cmd.length < 2) {
            printer.error("Missing arguments for bookmark command.");
        } else {
            switch (cmd[1]) {
                case "add" -> {
                    if (cmd.length == 4) {
                        handleAddBookmark(cmd[2], cmd[3]);
                    } else {
                        printer.error("'bookmark add' sub-command takes 2 arguments.");
                    }
                }
                case "rm" -> {
                    if (cmd.length == 3) {
                        handleRemoveBookmark(cmd[2]);
                    } else {
                        printer.error("'bookmark rm' sub-command takes only one argument.");
                    }
                }
                case "show" -> {
                    if (cmd.length == 2) {
                        handleShowBookmarks();
                    } else {
                        printer.error("'bookmark show' sub-command does not take any arguments.");
                    }
                }
                case "go" -> {
                    if (cmd.length == 3) {
                        handleGoToBookmark(cmd[2]);
                    } else {
                        printer.error("'bookmark go' sub-command takes 1 argument.");
                    }
                }
                default -> {
                    if (cmd.length == 2) {
                        handleGoToBookmark(cmd[1]);
                    } else {
                        printer.error("Invalid sub-command: " + cmd[1]);
                    }
                }
            }
        }
    }

    private void handleAddBookmark(String name, String url) {
        errorHandler.run(() -> {
            var done = bookmarks.add(name, url);
            if (done) printer.info("Bookmark added.");
            else printer.warn("Bookmark already exists. To change it, first remove it, then add it again.");
            return null;
        });
    }

    private void handleRemoveBookmark(String name) {
        errorHandler.run(() -> {
            var done = bookmarks.remove(name);
            if (done) printer.info("Bookmark removed.");
            else printer.info("Bookmark does not exist.");
            return null;
        });
    }

    private void handleShowBookmarks() {
        var all = bookmarks.getAll();
        printer.info("You have " + all.size() + " bookmark" +
                (all.size() == 1 ? "" : "s") +
                (all.size() == 0 ? ".\nTo add one, type '.bm add <name> <url>'." : ":\n"));
        all.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> printer.info("* " + entry.getKey() + " - " + entry.getValue()));
    }

    private void handleGoToBookmark(String name) {
        bookmarks.get(name).ifPresentOrElse(
                uri -> {
                    uim.getHistory().add(uri);
                    client.sendRequest(uri);
                },
                () -> printer.error("bookmark does not exist: '" + name + "'."));
    }

    private void handleCerts(String[] cmd) {
        if (cmd.length < 3) {
            printer.error("certs command takes at least 2 arguments.");
            return;
        }
        switch (cmd[1]) {
            case "server" -> handleServerCerts(cmd);
            default -> printer.error("invalid argument at position 1: '" + cmd[1] +
                    "'. Only 'server' is currently supported.");
        }
    }

    private void handleServerCerts(String[] cmd) {
        switch (cmd[2]) {
            case "rm" -> {
                if (cmd.length > 3) {
                    removeServerCerts(Stream.of(cmd).skip(3));
                } else {
                    printer.error("'certs server rm' requires at least one host as argument.");
                }
            }
            case "clear" -> {
                if (cmd.length == 3) {
                    clearServerCerts();
                } else {
                    printer.error("'certs srver clear' does not accept arguments.");
                }
            }
            case "show" -> showServerCerts(Stream.of(cmd).skip(3).toList());
            case "hosts" -> {
                if (cmd.length == 3) {
                    showServerCertHosts();
                } else {
                    printer.error("'certs srver hosts' does not accept arguments.");
                }
            }
            default -> printer.error("Invalid sub-command: " + cmd[2]);
        }
    }

    private void removeServerCerts(Stream<String> hosts) {
        hosts.forEach(host -> errorHandler.run(() -> {
            if (!certificateStorage.remove(host)) {
                printer.warn("host '" + host + "' not found.");
            }
            return null;
        }));
    }

    private void clearServerCerts() {
        uim.promptUser("Are you sure you want to remove all server TLS certificates? [y/n]", answer -> {
            switch (answer) {
                case "y" -> {
                    return errorHandler.run(() -> {
                        certificateStorage.clean();
                        return true;
                    }).orElse(true);
                }
                case "n" -> {
                    return true;
                }
                default -> {
                    printer.error("Please enter 'y' or 'n'.");
                    return false;
                }
            }
        });
    }

    private void showServerCerts(List<String> hosts) {
        errorHandler.run(() -> {
            var hostKeys = new HashSet<>(certificateStorage.loadAll().keySet());
            if (!hosts.isEmpty()) {
                hostKeys.retainAll(hosts);
                for (String host : hosts) {
                    if (!hostKeys.contains(host)) {
                        printer.warn("No certificate found for host '" + host + "'.");
                    }
                }
            }
            if (hostKeys.isEmpty()) {
                printer.error("No certificates found for hosts.");
            } else {
                for (String hostKey : hostKeys.stream().sorted().toList()) {
                    certificateStorage.load(hostKey).ifPresent(certificate ->
                            printer.info("Certificate for host '" + hostKey + "':\n" + certificate));
                }
            }
            return null;
        });
    }

    private void showServerCertHosts() {
        errorHandler.run(() -> {
            var hosts = certificateStorage.loadAll().keySet().stream().sorted().toList();
            if (hosts.isEmpty()) {
                printer.info("You have not cached any certificate yet.");
            } else {
                printer.info("You have cached certificates for the following " + hosts.size() + " host" +
                        (hosts.size() == 1 ? "" : "s") + ":");
                for (String host : hosts) {
                    printer.info("  * " + host);
                }
            }
            return null;
        });
    }

}