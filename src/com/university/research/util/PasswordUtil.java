package com.university.research.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltBase64) {
        if (password == null) throw new IllegalArgumentException("Password is required.");
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(encoded);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    public static boolean matches(String rawPassword, String salt, String expectedHash) {
        String actual = hash(rawPassword, salt);
        if (actual.length() != expectedHash.length()) return false;
        int diff = 0;
        for (int i = 0; i < actual.length(); i++) diff |= actual.charAt(i) ^ expectedHash.charAt(i);
        return diff == 0;
    }
}
