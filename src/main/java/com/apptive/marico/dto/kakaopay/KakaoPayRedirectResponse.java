package com.apptive.marico.dto.kakaopay;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayRedirectResponse {
    private Long applicationId;
    private String redirectPcUrl;
    private String redirectMobileUrl;
    private String redirectAppUrl;
}
