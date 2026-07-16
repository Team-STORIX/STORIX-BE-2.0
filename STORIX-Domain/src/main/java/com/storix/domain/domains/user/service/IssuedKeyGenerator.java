package com.storix.domain.domains.user.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class IssuedKeyGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generatePendingId() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
