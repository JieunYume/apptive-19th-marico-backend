package com.apptive.marico.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ServiceScheduleBookedEvent {
    private final String stylistEmail;
    private final String memberName;
    private final String serviceName;
    private final LocalDateTime scheduledAt;
}
