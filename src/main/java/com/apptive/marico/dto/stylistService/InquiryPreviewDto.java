package com.apptive.marico.dto.stylistService;


import com.apptive.marico.entity.service.ServiceInquiry;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class InquiryPreviewDto {
    private Long inquiryId;
    private String title;
    private String content;
    private boolean answerComplete;

    public static InquiryPreviewDto toDto(ServiceInquiry serviceInquiry) {
        return InquiryPreviewDto.builder()
                .inquiryId(serviceInquiry.getId())
                .title(serviceInquiry.getTitle())
                .content(serviceInquiry.getContent())
                .answerComplete(serviceInquiry.isAnswerComplete())
                .build();
    }

    @Builder
    @Getter
    public static class DtoList {
        List<InquiryPreviewDto> inquiryPreviewDtoList;

        public static InquiryPreviewDto.DtoList toDto(List<ServiceInquiry> serviceInquiryList) {
            List<InquiryPreviewDto> inquiryPreviewDtoList = serviceInquiryList.stream()
                    .map(InquiryPreviewDto::toDto)
                    .collect(Collectors.toList());

            return InquiryPreviewDto.DtoList.builder()
                    .inquiryPreviewDtoList(inquiryPreviewDtoList)
                    .build();
        }
    }
}