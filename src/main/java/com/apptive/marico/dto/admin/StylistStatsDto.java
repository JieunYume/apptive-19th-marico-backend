package com.apptive.marico.dto.admin;

import com.apptive.marico.entity.StylistStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StylistStatsDto {

    private Long stylistId;
    private String stageName;
    private String profileImage;
    private long totalOrderCount;
    private long totalRevenue;
    private long totalClientCount;
    private LocalDateTime lastMatchingDate;

    public static StylistStatsDto from(StylistStats stats) {
        return StylistStatsDto.builder()
                .stylistId(stats.getStylistId())
                .stageName(stats.getStageName())
                .profileImage(stats.getProfileImage())
                .totalOrderCount(stats.getTotalOrderCount())
                .totalRevenue(stats.getTotalRevenue())
                .totalClientCount(stats.getTotalClientCount())
                .lastMatchingDate(stats.getLastMatchingDate())
                .build();
    }
}
