package com.university.research.web;

import com.sun.net.httpserver.HttpExchange;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final String COOKIE_NAME = "URSESSION";
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    public String createSession(int userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, userId);
        return token;
    }

    public Optional<Integer> currentUserId(HttpExchange exchange) {
        return tokenFrom(exchange).map(sessions::get).filter(id -> id != null);
    }

    public void setSessionCookie(HttpExchange exchange, String token) {
        String secure = "https".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Forwarded-Proto")) ? "; Secure" : "";
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=28800" + secure);
    }

    public void logout(HttpExchange exchange) {
        tokenFrom(exchange).ifPresent(sessions::remove);
        String secure = "https".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Forwarded-Proto")) ? "; Secure" : "";
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0" + secure);
    }

    private Optional<String> tokenFrom(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) return Optional.empty();
        for (String part : cookieHeader.split(";")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && COOKIE_NAME.equals(pair[0])) return Optional.of(pair[1]);
        }
        return Optional.empty();
    }
}
