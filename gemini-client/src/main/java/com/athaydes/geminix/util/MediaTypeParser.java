package com.athaydes.geminix.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MediaTypeParser {

    public Optional<MediaType> parse(String mediaType) {
        if (mediaType.isEmpty()) return Optional.empty();
        var parts = mediaType.split(";");
        if (parts.length == 0) return Optional.empty();
        var typeSubtype = parts[0].split("/", 2);
        var type = typeSubtype.length > 0 ? typeSubtype[0].trim().toLowerCase(Locale.ROOT) : "";
        var subType = typeSubtype.length > 1 ? typeSubtype[1].trim().toLowerCase(Locale.ROOT) : "";

        var params = parseParams(parts);

        return Optional.of(new MediaType(type, subType, params));
    }

    private static Map<String, String> parseParams(String[] parts) {
        if (parts.length < 2) return Map.of();
        var result = new HashMap<String, String>(parts.length - 1);
        for (int i = 1; i < parts.length; i++) {
            var components = parts[i].split("=", 2);
            var key = components.length > 0 ? components[0].trim() : "";
            var value = components.length > 1 ? components[1].trim() : "";
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

}
