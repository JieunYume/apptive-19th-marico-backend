package com.apptive.marico.controller;

import com.apptive.marico.service.KakaoPayService;
import com.apptive.marico.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/kakao")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    @GetMapping("/success")
    public ResponseEntity<?> success(
            @RequestParam("matching_id") Long matchingId,
            @RequestParam("pg_token") String pgToken
    ) {
        return ResponseEntity.ok(ApiUtils.success(kakaoPayService.approve(matchingId, pgToken)));
    }

    @GetMapping("/fail")
    public ResponseEntity<?> fail() {
        return ResponseEntity.badRequest().body(ApiUtils.fail(400, "결제에 실패했습니다."));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancel() {
        return ResponseEntity.ok(ApiUtils.success("결제가 취소되었습니다."));
    }
}
