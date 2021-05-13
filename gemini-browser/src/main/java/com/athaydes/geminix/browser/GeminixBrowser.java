package com.athaydes.geminix.browser;

import com.athaydes.geminix.browser.internal.BrowserUserInteractionManager;
import com.athaydes.geminix.browser.internal.GeminiURL;
import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
import com.athaydes.geminix.text.GemTextLine;
import com.athaydes.geminix.text.GemTextParser;
import com.athaydes.geminix.util.MediaType;
import com.athaydes.geminix.util.MediaTypeParser;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GeminixBrowser extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final Button backButton = with(new Button("Back"), back -> {
        back.setFocusTraversable(false);
    });

    private final Button forwardButton = with(new Button("Forward"), forward -> {
        forward.setFocusTraversable(false);
    });

    private final TextField urlInputField = with(new TextField(), (field) -> {
        field.getStyleClass().add("url-input");
        field.setOnAction(event -> goTo(field.getText()));
    });

    private final WebView content = with(new WebView(), content -> {
        content.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("New location: " + newValue);
            urlInputField.setText(newValue);
        });
        content.getEngine().loadContent("""
                <html>
                <body>
                <h1>Welcome to the GeminiX Browser!</h1>
                <p>Welcome to the Geminix Browser!</p>
                <ul>
                  <li><a href="gemini://gemini.circumlunar.space/">gemini://gemini.circumlunar.space/</a></li>
                  <li><a href="gemini://geminispace.info/">gemini://geminispace.info/</a></li>
                  <li><a href="https://google.se/">Google</a></li>
                </ul>
                </body>
                </html>
                """);
    });

    private final HBox urlBar = with(new HBox(5.0), urlBar -> {
        urlBar.setPadding(new Insets(10.0, 4.0, 10.0, 4.0));
        urlBar.getStyleClass().add("url-bar");
        urlBar.getChildren().addAll(
                backButton,
                forwardButton,
                urlInputField
        );
    });

    private final VBox root = with(new VBox(10.0), root -> {
        root.getChildren().addAll(
                urlBar,
                content
        );
    });

    static {
        // initialize the gemini protocol handler so the webview can handle gemini:// links
        GeminiURL.init();
    }

    private final GemTextParser gemTextParser = new GemTextParser();
    private final MediaTypeParser mediaTypeParser = new MediaTypeParser();

    private final UserInteractionManager uim = new BrowserUserInteractionManager(response -> {
        if (response instanceof Response.Success success) {
            var mediaType = mediaTypeParser
                    .parse(success.mediaType())
                    .orElse(MediaType.GEMINI_TEXT);
            if (mediaType.isText()) {
                showSuccessText(mediaType, success);
            } else {
                System.err.println("TODO : cannot yet handle non-textual media-type");
            }
        } else {
            System.err.println("TODO: cannot handle this type yet: " + response);
        }
    });

    private void showSuccessText(MediaType mediaType, Response.Success success) {
        var charsetText = mediaType.getParameter(MediaType.Params.CHARSET)
                .orElse(StandardCharsets.UTF_8.name());

        Charset charset;
        if (Charset.isSupported(charsetText)) {
            charset = Charset.forName(charsetText);
        } else {
//            printer.warn("Unsupported charset: '" + charsetText + "', will fallback to UTF-8.");
            charset = StandardCharsets.UTF_8;
        }

        var reader = new BufferedReader(new InputStreamReader(success.body(), charset), 1024);

        var page = gemTextParser.apply(reader.lines())
                .map(this::geminiTextToHtml)
                .collect(Collectors.joining("\n"));

        content.getEngine().loadContent(page);
    }

    private final Client client = new Client(uim);

    @Override
    public void start(Stage stage) {
        var scene = new Scene(root, 600.0, 500.0);
        stage.setTitle("Geminix Browser");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void goTo(String uri) {
        content.getEngine().loadContent(null);
        client.sendRequest(uri);
    }

    private String geminiTextToHtml(GemTextLine line) {
        if (line instanceof GemTextLine.Text text) {
            return "<p>" + text.value() + "</p>";
        }
        if (line instanceof GemTextLine.PreformattedStart) {
            return "<pre>";
        }
        if (line instanceof GemTextLine.Preformatted pre) {
            return pre.value();
        }
        if (line instanceof GemTextLine.PreformattedEnd) {
            return "</pre>";
        }
        if (line instanceof GemTextLine.ListItem item) {
            return "<ul><li>" + item.value() + "</li></ul>";
        }
        if (line instanceof GemTextLine.Quote quote) {
            return "<quote>" + quote.value() + "</quote>";
        }
        if (line instanceof GemTextLine.Heading1 h1) {
            return "<h1>" + h1.value() + "</h1>";
        }
        if (line instanceof GemTextLine.Heading2 h2) {
            return "<h2>" + h2.value() + "</h2>";
        }
        if (line instanceof GemTextLine.Heading3 h3) {
            return "<h3>" + h3.value() + "</h3>";
        }
        if (line instanceof GemTextLine.Link link) {
            var description = link.description().isBlank() ? link.url() : link.description();
            return "<a href=\"" + link.url() + "\">" + description + "</a>";
        }
        return "";
    }

    private static <T> T with(T value, Consumer<T> action) {
        action.accept(value);
        return value;
    }

}
