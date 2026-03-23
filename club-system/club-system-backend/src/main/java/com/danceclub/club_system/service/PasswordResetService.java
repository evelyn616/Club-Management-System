package com.danceclub.club_system.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private final ConcurrentHashMap<String, ResetEntry> codeStore = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    public String generateAndStore(String email) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        codeStore.put(email, new ResetEntry(code, LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES), 0));
        return code;
    }

    public boolean verify(String email, String inputCode) {
        ResetEntry entry = codeStore.get(email);
        if (entry == null) return false;

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            codeStore.remove(email);
            return false;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            codeStore.remove(email);
            throw new IllegalStateException("驗證嘗試次數過多，請重新發送驗證碼");
        }

        boolean match = entry.code().equals(inputCode.trim());
        if (match) {
            codeStore.remove(email);
        } else {
            codeStore.put(email, new ResetEntry(entry.code(), entry.expiresAt(), entry.attempts() + 1));
        }
        return match;
    }

    public void remove(String email) {
        codeStore.remove(email);
    }

    private record ResetEntry(String code, LocalDateTime expiresAt, int attempts) {}
}
