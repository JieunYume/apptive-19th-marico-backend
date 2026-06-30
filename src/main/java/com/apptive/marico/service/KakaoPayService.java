package com.apptive.marico.service;

import com.apptive.marico.dto.kakaopay.KakaoPayApproveResponse;
import com.apptive.marico.dto.kakaopay.KakaoPayReadyResponse;
import com.apptive.marico.dto.kakaopay.KakaoPayRedirectResponse;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.ServiceMatchingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static com.apptive.marico.exception.ErrorCode.KAKAO_PAY_FAILED;
import static com.apptive.marico.exception.ErrorCode.STYLIST_MATCHING_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    @Value("${kakao.pay.approval-url}")
    private String approvalUrl;

    @Value("${kakao.pay.fail-url}")
    private String failUrl;

    @Value("${kakao.pay.cancel-url}")
    private String cancelUrl;

    private static final String KAKAO_PAY_READY_URL = "https://kapi.kakao.com/v1/payment/ready";
    private static final String KAKAO_PAY_APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";

    private final RestTemplate restTemplate;
    private final ServiceMatchingRepository orderServiceRepository;

    public KakaoPayRedirectResponse ready(Long matchingId, String userId, String itemName, int totalAmount) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", String.valueOf(matchingId));
        params.add("partner_user_id", userId);
        params.add("item_name", itemName);
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(totalAmount));
        params.add("vat_amount", "0");
        params.add("tax_free_amount", "0");
        params.add("approval_url", approvalUrl + "?matching_id=" + matchingId);
        params.add("fail_url", failUrl);
        params.add("cancel_url", cancelUrl);

        KakaoPayReadyResponse response = restTemplate.postForObject(
                KAKAO_PAY_READY_URL,
                new HttpEntity<>(params, buildHeaders()),
                KakaoPayReadyResponse.class
        );

        if (response == null) {
            throw new CustomException(KAKAO_PAY_FAILED);
        }

        ServiceMatching matching = orderServiceRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(STYLIST_MATCHING_NOT_FOUND));
        matching.ready(response.getTid());

        return KakaoPayRedirectResponse.builder()
                .applicationId(matchingId)
                .redirectPcUrl(response.getNextRedirectPcUrl())
                .redirectMobileUrl(response.getNextRedirectMobileUrl())
                .redirectAppUrl(response.getNextRedirectAppUrl())
                .build();
    }

    @Transactional
    public String approve(Long matchingId, String pgToken) {
        ServiceMatching matching = orderServiceRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tid", matching.getTid());
        params.add("partner_order_id", String.valueOf(matchingId));
        params.add("partner_user_id", matching.getMember().getUserId());
        params.add("pg_token", pgToken);

        KakaoPayApproveResponse response = restTemplate.postForObject(
                KAKAO_PAY_APPROVE_URL,
                new HttpEntity<>(params, buildHeaders()),
                KakaoPayApproveResponse.class
        );

        if (response == null) {
            throw new CustomException(KAKAO_PAY_FAILED);
        }

        matching.waitingForApproval();
        matching.completePayment(LocalDateTime.now(), response.getAmount().getTotal());

        return "결제가 완료되었습니다.";
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
