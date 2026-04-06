/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.security;

import java.security.SecureRandom;

public record SecurityConfig(String secretKey) {
    private static final String SECRET_KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static boolean isValid(String string) {
        if (string.isEmpty()) {
            return false;
        }
        return string.matches("^[a-zA-Z0-9]{40}$");
    }

    public static String generateSecretKey() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(40);
        for (int i = 0; i < 40; ++i) {
            stringBuilder.append(SECRET_KEY_CHARS.charAt(secureRandom.nextInt(SECRET_KEY_CHARS.length())));
        }
        return stringBuilder.toString();
    }
}

