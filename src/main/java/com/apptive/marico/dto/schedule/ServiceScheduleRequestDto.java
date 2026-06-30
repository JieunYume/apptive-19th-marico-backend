package com.apptive.marico.dto.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceScheduleRequestDto {

    @NotNull
    private Long matchingId;

    @NotNull
    private Long serviceContentId;

    @NotNull
    private LocalDateTime scheduledAt;
}
