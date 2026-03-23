package com.danceclub.club_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPaymentReminder(String toEmail, String userName, String activityTitle, BigDecimal amount, LocalDateTime paymentDeadline){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("way911206@gmail.com");
            message.setTo(toEmail);
            message.setSubject("【繳費提醒】"+ activityTitle);

            String deadlineStr = "";
            if(paymentDeadline != null){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                deadlineStr = "\n繳費期限："+ formatter.format(paymentDeadline);
            }

            String content = String.format(
                    "%s，您好：\n\n"+
                    "這是一封繳費提醒信件。\n\n" +
                    "活動名稱：%s\n" +
                    "應繳金額：NT$ %s%s\n\n" +
                    "請儘速完成繳費，謝謝！\n\n" +
                    "繳費方式：\n" +
                    "1. ATM 轉帳\n" +
                    "2. 超商代碼繳費\n" +
                    "3. 信用卡線上繳費\n\n" +
                    "此為系統自動發送郵件，請勿直接回覆。",
                    userName,activityTitle,amount, deadlineStr
            );

            message.setText(content);
            mailSender.send(message);
        }
        catch (Exception e){
            throw new RuntimeException("發送信件失敗"+ e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("way911206@gmail.com");
            message.setTo(toEmail);
            message.setSubject("【密碼重設】驗證碼");
            message.setText("您的密碼重設驗證碼為：\n\n    " + code +
                    "\n\n此驗證碼將於 5 分鐘後失效，請勿洩漏給他人。\n\n若非本人操作，請忽略此信。\n\n── Club System 安全系統");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("發送密碼重設信件失敗：" + e.getMessage());
        }
    }
}
