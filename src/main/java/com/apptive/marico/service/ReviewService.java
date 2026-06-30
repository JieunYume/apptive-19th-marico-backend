package com.apptive.marico.service;

import com.apptive.marico.dto.review.ReviewRequestDto;
import com.apptive.marico.dto.review.ReviewResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.service.ServiceReview;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ReviewRepository;
import com.apptive.marico.repository.StylistRepository;
import com.apptive.marico.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.apptive.marico.exception.ErrorCode.SERVICE_NOT_FOUND;
import static com.apptive.marico.exception.ErrorCode.USER_NOT_FOUND;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StylistRepository stylistRepository;
    private final ServiceRepository stylistServiceRepository;
    private final MemberRepository memberRepository;

    public List<ReviewResponseDto> getReviewsByService(Long serviceId) {
        if (!stylistServiceRepository.existsById(serviceId)) {
            throw new CustomException(SERVICE_NOT_FOUND);
        }
        return reviewRepository.findAllByServiceId(serviceId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDto createReview(String userId, Long serviceId, ReviewRequestDto requestDto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Service stylistService = stylistServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(SERVICE_NOT_FOUND));

        ServiceReview review = ServiceReview.builder()
                .member(member)
                .stylistService(stylistService)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        // 리뷰가 달린 서비스의 스타일리스트 review_count 반정규화 값 증가
        stylistRepository.addToReviewCount(stylistService.getStylist().getId(), 1);

        return ReviewResponseDto.from(review);
    }
//
//    public List<ReviewResponseDto> getReviewsByStylist(Long stylistId) {
//        if (!stylistRepository.existsById(stylistId)) {
//            throw new CustomException(STYLIST_NOT_FOUND);
//        }
//        return reviewRepository.findAllByStylistId(stylistId).stream()
//                .map(ReviewResponseDto::from)
//                .collect(Collectors.toList());
//    }
}
