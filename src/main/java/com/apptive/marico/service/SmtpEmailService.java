package com.apptive.marico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("인증코드 입니다.");
        message.setText("Your verification code is " + verificationCode);
        mailSender.send(message);
    }

    public void sendScheduleNotification(String toEmail, String memberName, String serviceName, java.time.LocalDateTime scheduledAt) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        String text = String.format(
                "[마리코] 회원 %s님이 '%s' 서비스의 일정을 예약했습니다.\n일시: %s",
                memberName,
                serviceName,
                scheduledAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[마리코] 새로운 일정 예약 알림");
        message.setText(text);
        mailSender.send(message);
    }
}