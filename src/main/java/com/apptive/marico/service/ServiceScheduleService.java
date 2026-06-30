package com.apptive.marico.service;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.dto.schedule.ServiceScheduleResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.ServiceSchedule;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ServiceMatchingRepository;
import com.apptive.marico.repository.ServiceCategoryRepository;
import com.apptive.marico.repository.ServiceScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.apptive.marico.exception.ErrorCode.*;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServiceScheduleService {

    private final MemberRepository memberRepository;
    private final ServiceMatchingRepository orderServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceScheduleRepository serviceScheduleRepository;

    @Transactional
    public ServiceScheduleResponseDto bookSchedule(String userId, Long matchingId, Long serviceContentId, ServiceScheduleRequestDto dto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        if (!matching.getMember().getId().equals(member.getId())) {
            throw new CustomException(MATCHING_MEMBER_NOT_MATCH);
        }

        if (!"DONE".equals(matching.getApprovalStatus())) {
            throw new CustomException(MATCHING_NOT_DONE);
        }

        ServiceContent serviceContent = serviceCategoryRepository.findById(serviceContentId)
                .orElseThrow(() -> new CustomException(SERVICE_CONTENT_NOT_FOUND));

        if (!serviceContent.getStylistService().getId().equals(matching.getService().getId())) {
            throw new CustomException(SERVICE_CONTENT_NOT_IN_SERVICE);
        }

        if (serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching, serviceContent)) {
            throw new CustomException(SCHEDULE_ALREADY_EXISTS);
        }

        ServiceSchedule schedule = ServiceSchedule.builder()
                .serviceMatching(matching)
                .serviceContent(serviceContent)
                .scheduledAt(dto.getScheduledAt())
                .location(dto.getLocation())
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        return ServiceScheduleResponseDto.from(serviceScheduleRepository.save(schedule));
    }
}
