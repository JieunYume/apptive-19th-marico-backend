package com.apptive.marico.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class FakeEmailService {

    public void sendScheduleNotification(String toEmail, String memberName, String serviceName, LocalDateTime scheduledAt) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        long delay = ThreadLocalRandom.current().nextLong(200, 501);

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[FakeEmail] {}ms 소요 | 수신: {} | 내용: {}님이 '{}' 서비스를 {} 에 예약",
                delay,
                toEmail,
                memberName,
                serviceName,
                scheduledAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
