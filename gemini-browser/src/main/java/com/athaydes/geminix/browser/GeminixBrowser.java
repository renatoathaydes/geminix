package com.athaydes.geminix.browser;

import com.athaydes.geminix.browser.internal.BrowserUserInteractionManager;
import com.athaydes.geminix.browser.internal.GeminiURL;
import com.athaydes.geminix.browser.internal.TextResponseReader;
import com.athaydes.geminix.client.Client;
import com.athaydes.geminix.client.Response;
import com.athaydes.geminix.client.UserInteractionManager;
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

import java.util.function.Consumer;

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

    private final MediaTypeParser mediaTypeParser = new MediaTypeParser();

    private final TextResponseReader textResponseReader = new TextResponseReader();

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

    private final Client client = new Client(uim);

    {
        // initialize the gemini protocol handler so the webview can handle gemini:// links
        GeminiURL.init(new GeminiURL.Dependencies(mediaTypeParser, textResponseReader));
    }

    private void showSuccessText(MediaType mediaType, Response.Success success) {
        String page = textResponseReader.readBody(mediaType, success);
        content.getEngine().loadContent(page);
    }

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

    private static <T> T with(T value, Consumer<T> action) {
        action.accept(value);
        return value;
    }

}
