package com.apptive.marico.dto.review;

import com.apptive.marico.entity.service.ServiceReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private Long reviewId;
    private String memberNickname;
    private String memberProfileImage;
    private String serviceName;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponseDto from(ServiceReview review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .memberNickname(review.getMember().getNickname())
                .memberProfileImage(review.getMember().getProfileImage())
                .serviceName(review.getStylistService().getServiceName())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
