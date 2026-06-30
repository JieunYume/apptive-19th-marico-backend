package com.apptive.marico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAlimTalkService {

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Value("${kakao.alimtalk.sender-key}")
    private String senderKey;

    @Value("${kakao.alimtalk.template-code}")
    private String templateCode;

    private static final String ALIM_TALK_URL = "https://api-alimtalk.kakao.com/v2/services/";

    private final RestTemplate restTemplate;

    public void sendScheduleNotification(String phoneNumber, String memberName, String serviceName, LocalDateTime scheduledAt) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("스타일리스트 전화번호가 없어 알림톡을 전송하지 않습니다.");
            return;
        }

        String content = String.format(
                "[마리코] 회원 %s님이 '%s' 서비스의 일정을 예약했습니다.\n일시: %s",
                memberName,
                serviceName,
                scheduledAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        Map<String, Object> body = Map.of(
                "senderKey", senderKey,
                "templateCode", templateCode,
                "messages", List.of(Map.of(
                        "to", phoneNumber,
                        "content", content
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForObject(
                    ALIM_TALK_URL + senderKey + "/messages",
                    new HttpEntity<>(body, headers),
                    String.class
            );
        } catch (Exception e) {
            log.error("알림톡 전송 실패: {}", e.getMessage());
        }
    }
}
