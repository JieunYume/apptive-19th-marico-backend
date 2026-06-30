package com.apptive.marico.dto.stylist.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMatchingApproveDto {

    private List<ScheduleItemDto> schedules;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItemDto {
        private Long serviceContentId;
        private LocalDateTime scheduledAt;
        private String location;
    }
}
