package com.apptive.marico.dto.schedule;

import com.apptive.marico.entity.service.ConnectionType;
import com.apptive.marico.entity.service.ServiceSchedule;
import com.apptive.marico.entity.service.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceScheduleResponseDto {

    private Long scheduleId;
    private Long matchingId;
    private Long serviceContentId;
    private ServiceType serviceType;
    private ConnectionType connectionType;
    private LocalDateTime scheduledAt;
    private String location;
    private String status;
    private LocalDateTime createdAt;

    public static ServiceScheduleResponseDto from(ServiceSchedule schedule) {
        return ServiceScheduleResponseDto.builder()
                .scheduleId(schedule.getId())
                .matchingId(schedule.getServiceMatching().getId())
                .serviceContentId(schedule.getServiceContent().getId())
                .serviceType(schedule.getServiceContent().getServiceType())
                .connectionType(schedule.getServiceContent().getConnectionType())
                .scheduledAt(schedule.getScheduledAt())
                .location(schedule.getLocation())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
