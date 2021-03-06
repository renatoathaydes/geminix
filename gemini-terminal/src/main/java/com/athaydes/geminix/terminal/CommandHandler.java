package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.text.GemTextLine.Link;
import com.athaydes.geminix.tls.TlsCertificateStorage;
import org.fusesource.jansi.Ansi;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

final class CommandHandler {
    private static final String HELP = """
            # Geminix Help
            
            Geminix is a Gemini Client.
                       
            You can use it to navigate the Geminspace, much like you can use a web browser to navigate the WWW.
                        
            To visit a URL, simply type it in. The scheme and port are optional.
            The following are example URIs you can try:
                        
            => gemini.circumlunar.space/
            => geminispace.info/
            => gemini://tobykurien.com/
            
            ## Commands
                        
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
            # Colors Command
            
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
            # Link Command
            
            The link (l) command is used to display the URL of, and follow links from the current page.
                        
            Links are displayed with an index between square brackets (e.g. [1]). To follow a link, pass the link \
            index as an argument to this command.
                        
            To display the URL a link refers to, write 'url' after the index. For example:
                        
            > .link 3 url
                        
            When invoked without an argument, the link command will display all available links.
            """;

    private static final String HELP_HELP = """
            # Help Command
            
            The help command can be called with zero or one argument.
                        
            Without an argument, it prints the Geminix main help page.
                        
            If given a command as an argument, it prints command-specific help.
            """;

    private static final String PROMPT_HELP = """
            # Prompt Command
            
            The prompt command changes the prompt which is displayed when Geminix expects user input.
            """;

    private static final String CERTS_HELP = """
            # Certs Command
            
            The certs command can be used to manage TLS certificates.
                        
            It accepts the following arguments:
                        
            * server show [hosts] - shows the certificate for one or more hosts,
                                    or for all hosts if no host is given.
            * server hosts        - shows all hosts for which a TLS certificate has been cached.
            * server rm <host>    - remove the cached certificate for the given host.
            * server clear        - remove all cached certificates for all hosts.
            """;

    private static final String BOOKMARK_HELP = """
            # Bookmark Command
            
            The bookmark (b) command is used to manage and use bookmarks.
                        
            It accepts the following arguments:
                        
            * add <name> [<url>] - create a bookmark.
            * rm <name>          - remove a bookmark.
            * show               - show all bookmarks.
            * go <name>          - send a request to the URL associated with the given bookmark.
            * <name>             - same as 'go <name>' as long as <name> is not a sub-command.
                        
            To open the URL associated with a bookmark, simply type '.bm <name>'.
                        
            For example:
                        
            ```
            # Add bookmark
            .b add gmn gemini.circumlunar.space/
                        
            # Navigate to the bookmarked URL
            .b gmn
            ```
                        
            If no URL is given to the add sub-command, the current URL is used.
            In other words, to bookmark the current URL, type `.b bookmark-name`.
            """;

    private static final String QUIT_HELP = """
            # Quit Command
            
            The quit (q) command exits Geminix.
            """;

    private static final String WIDTH_HELP = """
            # Width Command
            
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

    private void printGeminiText(String text) {
        uim.getGemTextParser().apply(text.lines()).forEach(printer::print);
    }

    private void handleHelp(String[] cmd) {
        switch (cmd.length) {
            case 1 -> printGeminiText(HELP);
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
            case "colors" -> printGeminiText(COLORS_HELP);
            case "help" -> printGeminiText(HELP_HELP);
            case "prompt" -> printGeminiText(PROMPT_HELP);
            case "bookmark" -> printGeminiText(BOOKMARK_HELP);
            case "certs" -> printGeminiText(CERTS_HELP);
            case "width" -> printGeminiText(WIDTH_HELP);
            case "link" -> printGeminiText(LINK_HELP);
            case "quit" -> printGeminiText(QUIT_HELP);
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
            printGeminiText("The current page is " + uim.getCurrentUrl() +
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
                uim.getErrorHandler().run(() -> handleLink(cmd, links.get(linkIndex), linkIndex));
            } else {
                printer.error("Bad argument, integer value is out of range 0-" + (links.size() - 1) + ".");
            }
        } else {
            printer.error("Too many arguments for link command.");
        }
    }

    private Object handleLink(String[] cmd, Link link, int linkIndex) throws URISyntaxException {
        var destination = client.getLinkDestination(uim.getCurrentUrl(), link);
        var isGemini = "gemini".equals(destination.getScheme());
        if (cmd.length == 3) {
            if (cmd[2].equalsIgnoreCase("url")) {
                printer.print(link, linkIndex);
                printer.print("  " + destination, printer.linkColor);
                if (!isGemini) {
                    printer.warn("Link cannot be followed as it does not use the gemini protocol.");
                }
            } else {
                printer.error("Expected 'url' after link index.");
            }
        } else if (isGemini) {
            uim.getHistory().add(destination.toString());
            client.sendRequest(destination);
        } else {
            printer.error("Cannot follow non-gemini link (protocol is '" + destination.getScheme() + "')");
        }
        return null;
    }

    private void handleBookmark(String[] cmd) {
        if (cmd.length < 2) {
            printer.error("Missing arguments for bookmark command.");
        } else {
            switch (cmd[1]) {
                case "add" -> {
                    switch (cmd.length) {
                        case 3 -> {
                            var url = uim.getCurrentUrl();
                            if (url == null) {
                                printer.error("No URL has been visited yet. Give an URL explicitly, or visit a URL " +
                                        "before bookmarking it.");
                            } else {
                                handleAddBookmark(cmd[2], url.toString());
                            }
                        }
                        case 4 -> handleAddBookmark(cmd[2], cmd[3]);
                        default -> printer.error("'bookmark add' sub-command takes 2 or 3 arguments.");
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
            else printer.warn("Bookmark name already exists. To change it, first remove it, then add it again.");
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
        printGeminiText("You have " + all.size() + " bookmark" +
                (all.size() == 1 ? "" : "s") +
                (all.size() == 0 ? ".\nTo add one, type '.bm add <name> <url>'." : ":\n"));
        all.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> printGeminiText("* " + entry.getKey() + " - " + entry.getValue()));
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
                            printGeminiText("Certificate for host '" + hostKey + "':\n" + certificate));
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
                printGeminiText("You have cached certificates for the following " + hosts.size() + " host" +
                        (hosts.size() == 1 ? "" : "s") + ":");
                for (String host : hosts) {
                    printGeminiText("* " + host);
                }
            }
            return null;
        });
    }

}