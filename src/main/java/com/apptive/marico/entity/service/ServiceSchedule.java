package com.apptive.marico.entity.service;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_matching_id")
    private ServiceMatching serviceMatching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_content_id")
    private ServiceContent serviceContent;

    private LocalDateTime scheduledAt;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
}
