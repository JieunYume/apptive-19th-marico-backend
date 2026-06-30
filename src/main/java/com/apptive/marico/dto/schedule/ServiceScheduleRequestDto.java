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
    private LocalDateTime scheduledAt;

    private String location;
}
