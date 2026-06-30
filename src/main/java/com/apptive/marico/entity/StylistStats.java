package com.apptive.marico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// findStylistStats 집계 결과를 주기적으로 미리 계산해두는 배치 테이블 (StylistStatsBatchService가 갱신)
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stylist_stats")
public class StylistStats {

    @Id
    @Column(name = "stylist_id")
    private Long stylistId;

    private String stageName;

    private String profileImage;

    @Column(nullable = false)
    private long totalOrderCount;

    @Column(nullable = false)
    private long totalRevenue;

    @Column(nullable = false)
    private long totalClientCount;

    private LocalDateTime lastMatchingDate;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
