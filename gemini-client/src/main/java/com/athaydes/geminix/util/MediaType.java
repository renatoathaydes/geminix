package com.athaydes.geminix.util;

import java.util.Map;
import java.util.Optional;

public record MediaType(String type, String subType, Map<String, String> parameters) {

    public sealed interface Params permits Params.None {
        enum None implements Params {}

        String CHARSET = "charset";
        String LANGUAGE = "lang";
    }

    public static final MediaType GEMINI_TEXT = new MediaType("text", "gemini",
            Map.of(Params.CHARSET, "UTF-8"));

    public boolean isText() {
        return "text".equals(type);
    }

    public boolean isGeminiText() {
        return isText() && "gemini".equals(subType);
    }

    public boolean isImage() {
        return "image".equals(type);
    }

    public boolean isAudio() {
        return "audio".equals(type);
    }

    public boolean isApplication() {
        return "application".equals(type);
    }

    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(parameters().get(name));
    }
}
