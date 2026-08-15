package com.university.research.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WebUtil {
    private WebUtil() { }

    public static Map<String, String> queryParams(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    public static Map<String, String> formParams(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return queryParams(body);
        }
    }

    public static int intParam(Map<String, String> params, String key) {
        try {
            return Integer.parseInt(params.getOrDefault(key, ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for " + key + ".");
        }
    }

    public static String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String encode(String input) {
        return URLEncoder.encode(input == null ? "" : input, StandardCharsets.UTF_8);
    }

    public static String decode(String input) {
        return URLDecoder.decode(input == null ? "" : input, StandardCharsets.UTF_8);
    }
}
