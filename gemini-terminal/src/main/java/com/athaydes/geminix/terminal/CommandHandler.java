package com.athaydes.geminix.terminal;

import com.athaydes.geminix.client.ErrorHandler;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.tls.TlsCertificateStorage;
import org.fusesource.jansi.Ansi;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.jline.builtins.Completers.TreeCompleter.node;

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
                        
            * help          - shows this help message.
            * help <cmd>    - show help for a given command.
            * colors <args> - manages output colors.
            * prompt <p>    - sets the prompt.
            * certs <args>  - manages TLS certificates.
            * quit          - quits Geminix.
                        
            To enter a command, prefix it with a '.'.
                        
            For example, to see help for the colors command, type:
                        
              .help colors
                        
            """;

    private static final String COLORS_HELP = """
            The 'colors' command accepts the following arguments:
                        
            * <level> <color>   - set the color to use for a log level.
            * off               - turn off terminal colors.
            * on                - turn on terminal colors.
                        
            Log levels are:
                        
            * info
            * warn
            * error
            * prompt
                        
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

    private static final String QUIT_HELP = """
            The quit command exits Geminix.
            """;

    private final TerminalPrinter printer;
    private final TlsCertificateStorage certificateStorage;
    private final ErrorHandler errorHandler;
    private final UserInteractionManager uim;

    public CommandHandler(CommandLineUserInteractionManager uim) {
        this.certificateStorage = uim.getCertificateStorage();
        this.printer = uim.getPrinter();
        this.errorHandler = uim.getErrorHandler();
        this.uim = uim;
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
                case "help":
                    handleHelp(cmd);
                    break;
                case "quit":
                    return true;
                case "colors":
                    handleColors(cmd);
                    break;
                case "prompt":
                    handlePrompt(answer.substring("prompt".length()));
                    break;
                case "certs":
                    handleCerts(cmd);
                    break;
                default:
                    printer.error("Invalid command: " + answer);
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
            case "prompt" -> printer.setPromptColor(ansiColor);
            default -> printer.error("Invalid log level: " + level);
        }
    }

    private void handleHelpFor(String cmd) {
        switch (cmd) {
            case "colors" -> printer.info(COLORS_HELP);
            case "help" -> printer.info(HELP_HELP);
            case "prompt" -> printer.info(PROMPT_HELP);
            case "certs" -> printer.info(CERTS_HELP);
            case "quit" -> printer.info(QUIT_HELP);
            default -> printer.error("Unknown command: " + cmd);
        }
    }

    private void handlePrompt(String text) {
        printer.setPrompt(text.trim() + " ");
    }

    private void handleCerts(String[] cmd) {
        if (cmd.length < 3) {
            printer.error("certs command takes at least 2 arguments");
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

    public Completer getCompleter() {
        var hostsCompleter = node(new StringsCompleter(() -> {
            try {
                return certificateStorage.loadAll().keySet().stream().toList();
            } catch (TlsCertificateStorage.StorageException e) {
                return Collections.emptyList();
            }
        }));
        return new Completers.TreeCompleter(
                node(".help",
                        node("help", "quit", "colors", "prompt", "certs")),
                node(".quit"),
                node(".colors",
                        node("on", "off"),
                        node("info", "warn", "error", "prompt",
                                node("black", "red", "green", "yellow", "blue", "magenta", "cyan", "white", "default"))),
                node(".prompt"),
                node(".certs",
                        node("server",
                                node("show", hostsCompleter),
                                node("rm", hostsCompleter),
                                "clear"))
        );
    }
}