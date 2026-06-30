package com.apptive.marico.dto.stylistService;


import com.apptive.marico.dto.stylist.service.StylistServiceDto;
import com.apptive.marico.entity.service.ServiceInquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class InquiryDto {

    private Long service_id;
    private String title;
    private String content;
    private List<String> img = new ArrayList<>();
    private String responseContent;
    private List<String> responseImg = new ArrayList<>();

    private boolean answerComplete = false;


    @Builder
    @Getter
    public static class InquiryResponseDto {
        private StylistServiceDto stylistServiceDto;
        private String title;
        private String content;
        private List<String> img = new ArrayList<>();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InquiryAnswerContentDto {
        private String responseContent;
    }

    public static InquiryDto toDto(ServiceInquiry serviceInquiry) {
        return InquiryDto.builder()
                .service_id(serviceInquiry.getId())
                .title(serviceInquiry.getTitle())
                .content(serviceInquiry.getContent())
                .img(serviceInquiry.getInquiryImg())
                .responseContent(serviceInquiry.getResponseContent())
                .responseImg(serviceInquiry.getResponseImg())
                .answerComplete(serviceInquiry.isAnswerComplete())
                .build();
    }
}
