package com.danceclub.club_system.service;

import com.danceclub.club_system.model.User;
import com.danceclub.club_system.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Value;          // TODO: 實作寄信時取消註解
// import org.springframework.mail.SimpleMailMessage;                  // TODO: 實作寄信時取消註解
// import org.springframework.mail.javamail.JavaMailSender;            // TODO: 實作寄信時取消註解
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    private static final Logger log = LoggerFactory.getLogger(MfaService.class);

    // private final JavaMailSender mailSender;  // TODO: 實作寄信時取消註解
    private final UserRepository userRepository;

    // @Value("${spring.mail.username}")         // TODO: 實作寄信時取消註解
    // private String fromEmail;

    // in-memory store: userId -> MfaEntry
    private final ConcurrentHashMap<String, MfaEntry> codeStore = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    public MfaService(/* JavaMailSender mailSender, */ UserRepository userRepository) {
        // this.mailSender = mailSender;  // TODO: 實作寄信時取消註解
        this.userRepository = userRepository;
    }

    /**
     * 發送 MFA 驗證碼
     * @param principal JWT subject（email）
     * @param type      "EMAIL" | "PHONE"
     */
    public void sendCode(String principal, String type) {
        User user = userRepository.findByEmail(principal)
                .orElseThrow(() -> new IllegalArgumentException("找不到使用者：" + principal));
        String userId = user.getId();

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException("非管理員帳號不可使用 MFA 驗證");
        }

        String code = generateCode();
        codeStore.put(userId, new MfaEntry(code, LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES), 0));

        if ("EMAIL".equalsIgnoreCase(type)) {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new IllegalStateException("此帳號尚未設定電子信箱");
            }
            // TODO: 實作 Email 發送 —— 取消以下註解並補上 JavaMailSender 依賴
            // sendEmail(user.getEmail(), user.getName(), code);
            log.info("[MFA-STUB] EMAIL 驗證碼 for userId={} → {}", userId, code);

        } else if ("PHONE".equalsIgnoreCase(type)) {
            if (user.getPhone() == null || user.getPhone().isBlank()) {
                throw new IllegalStateException("此帳號尚未設定手機號碼");
            }
            // TODO: 實作 SMS 發送 —— 加入 Twilio dependency 後取消以下註解
            // sendSms(user.getPhone(), code);
            log.info("[MFA-STUB] SMS 驗證碼 for userId={} → {}", userId, code);

        } else {
            throw new IllegalArgumentException("不支援的驗證方式：" + type);
        }
    }

    /**
     * 驗證碼驗證
     * @param principal JWT subject（email）
     * @return true = 正確；false = 錯誤或過期
     */
    public boolean verifyCode(String principal, String inputCode) {
        String userId = userRepository.findByEmail(principal)
                .map(u -> u.getId())
                .orElse(principal); // fallback（理論上不會發生）
        MfaEntry entry = codeStore.get(userId);
        if (entry == null) return false;

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            codeStore.remove(userId);
            return false;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            codeStore.remove(userId);
            throw new IllegalStateException("驗證嘗試次數過多，請重新發送驗證碼");
        }

        boolean match = entry.code().equals(inputCode.trim());
        if (match) {
            codeStore.remove(userId); // 一次性使用
        } else {
            codeStore.put(userId, new MfaEntry(entry.code(), entry.expiresAt(), entry.attempts() + 1));
        }
        return match;
    }

    /**
     * 取得遮罩後的電子信箱（用於前端提示）
     */
    public String getMaskedEmail(String principal) {
        return userRepository.findByEmail(principal)
                .map(u -> maskEmail(u.getEmail()))
                .orElse("***@***.***");
    }

    /**
     * 取得遮罩後的手機號碼（用於前端提示）
     */
    public String getMaskedPhone(String principal) {
        return userRepository.findByEmail(principal)
                .map(u -> maskPhone(u.getPhone()))
                .orElse("***-***-****");
    }

    // =========================================================
    //  Private helpers
    // =========================================================

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // TODO: 實作 Email 發送時取消以下整段註解，並還原 constructor 的 JavaMailSender 注入
    /*
    private void sendEmail(String toEmail, String userName, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("【管理員驗證碼】Dance Club System");
        message.setText(String.format(
                "%s，您好：\n\n" +
                "您的管理員登入驗證碼為：\n\n" +
                "    %s\n\n" +
                "此驗證碼將於 %d 分鐘後失效，且僅限使用一次。\n\n" +
                "若非本人操作，請立即修改密碼並聯絡系統管理員。\n\n" +
                "── Dance Club System 安全系統",
                userName, code, CODE_EXPIRY_MINUTES
        ));
        mailSender.send(message);
    }
    */

    // TODO: 實作 SMS 發送時取消以下整段註解，並加入 Twilio dependency
    // 參考 deployment.md 的「MFA — 手機簡訊」章節
    /*
    private void sendSms(String phone, String code) {
        // Twilio.init(accountSid, authToken);
        // Message.creator(
        //     new PhoneNumber(phone),
        //     new PhoneNumber(fromNumber),
        //     "【Dance Club】管理員驗證碼：" + code + "（" + CODE_EXPIRY_MINUTES + " 分鐘內有效）"
        // ).create();
    }
    */

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***.***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        String masked = local.length() <= 2
                ? local.charAt(0) + "***"
                : local.substring(0, 2) + "*".repeat(Math.min(local.length() - 2, 4));
        return masked + "@" + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    // =========================================================
    //  Inner record
    // =========================================================

    private record MfaEntry(String code, LocalDateTime expiresAt, int attempts) {}
}
