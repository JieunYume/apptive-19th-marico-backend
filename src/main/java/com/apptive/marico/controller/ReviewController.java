package com.apptive.marico.controller;

import com.apptive.marico.dto.review.ReviewRequestDto;
import com.apptive.marico.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getReviewsByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.getReviewsByService(serviceId));
    }

    @PostMapping("/service/{serviceId}")
    public ResponseEntity<?> createReview(
            Principal principal,
            @PathVariable Long serviceId,
            @Valid @RequestBody ReviewRequestDto requestDto
    ) {
        return ResponseEntity.ok(reviewService.createReview(principal.getName(), serviceId, requestDto));
    }

//    @GetMapping("/stylist/{stylistId}")
//    public ResponseEntity<?> getReviewsByStylist(@PathVariable Long stylistId) {
//        return ResponseEntity.ok(reviewService.getReviewsByStylist(stylistId));
//    }
}
