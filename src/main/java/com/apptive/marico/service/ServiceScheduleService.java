package com.apptive.marico.service;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.dto.schedule.ServiceScheduleResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.ServiceSchedule;
import com.apptive.marico.event.ServiceScheduleBookedEvent;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ServiceMatchingRepository;
import com.apptive.marico.repository.ServiceContentRepository;
import com.apptive.marico.repository.ServiceScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.apptive.marico.exception.ErrorCode.*;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServiceScheduleService {

    private final MemberRepository memberRepository;
    private final ServiceMatchingRepository orderServiceRepository;
    private final ServiceContentRepository serviceCategoryRepository;
    private final ServiceScheduleRepository serviceScheduleRepository;
    private final FakeEmailService smtpEmailService;
    private final ApplicationEventPublisher eventPublisher;

    // 트랜잭션이 끝나면 matching/member/service는 준영속 상태가 되어 지연 로딩 필드에 접근할 수 없으므로,
    // 알림 발송(Facade)에 필요한 값은 트랜잭션 안에서 미리 꺼내 함께 반환한다.
    public record ScheduleBookingResult(ServiceScheduleResponseDto response, String stylistEmail, String memberName, String serviceName) {}

    private record ValidatedBooking(Member member, ServiceMatching matching, ServiceSchedule schedule) {}

    // 세 가지 방식이 공통으로 쓰는 검증 + 저장 로직
    private ValidatedBooking validateAndSave(String userId, ServiceScheduleRequestDto dto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findById(dto.getMatchingId())
                .orElseThrow(() -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        if (!matching.getMember().getId().equals(member.getId())) {
            throw new CustomException(MATCHING_MEMBER_NOT_MATCH);
        }

        if (!"DONE".equals(matching.getApprovalStatus())) {
            throw new CustomException(MATCHING_NOT_DONE);
        }

        ServiceContent serviceContent = serviceCategoryRepository.findById(dto.getServiceContentId())
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
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        return new ValidatedBooking(member, matching, serviceScheduleRepository.save(schedule));
    }

    // 1. 원래 코드: 검증 + 저장 + 이메일 발송을 한 트랜잭션 안에서 동기적으로 전부 처리한다.
    //    이메일 전송이 오래 걸리면 그만큼 DB 커넥션을 붙잡고 있게 된다.
    @Transactional
    public ServiceScheduleResponseDto bookSchedule(String userId, ServiceScheduleRequestDto dto) {
        ValidatedBooking booking = validateAndSave(userId, dto);

        smtpEmailService.sendScheduleNotification(
                booking.matching().getService().getStylist().getEmail(),
                booking.member().getName(),
                booking.matching().getService().getServiceName(),
                dto.getScheduledAt()
        );

        return ServiceScheduleResponseDto.from(booking.schedule());
    }

    // 2. 트랜잭션과 메일 전송을 분리한 코드: 여기서는 DB 작업만 하고, 알림 발송은 Facade가 트랜잭션 밖에서 처리한다.
    @Transactional
    public ScheduleBookingResult bookScheduleWithTransaction(String userId, ServiceScheduleRequestDto dto) {
        ValidatedBooking booking = validateAndSave(userId, dto);

        return new ScheduleBookingResult(
                ServiceScheduleResponseDto.from(booking.schedule()),
                booking.matching().getService().getStylist().getEmail(),
                booking.member().getName(),
                booking.matching().getService().getServiceName()
        );
    }

    // 3. event로 개선한 코드: 이벤트 발행은 반드시 이 트랜잭션 "안"에서 이뤄져야 한다.
    //    @TransactionalEventListener(AFTER_COMMIT)는 publishEvent 시점에 활성 트랜잭션이 있어야
    //    커밋 이후 콜백을 등록할 수 있고, 없으면 이벤트가 그냥 버려진다.
    //    실제 이메일 전송(느린 작업)은 리스너에서 커밋 이후 비동기로 처리되므로 이 트랜잭션은 짧게 끝난다.
    @Transactional
    public ServiceScheduleResponseDto improvedBookScheduleWithTransaction(String userId, ServiceScheduleRequestDto dto) {
        ValidatedBooking booking = validateAndSave(userId, dto);

        eventPublisher.publishEvent(new ServiceScheduleBookedEvent(
                booking.matching().getService().getStylist().getEmail(),
                booking.member().getName(),
                booking.matching().getService().getServiceName(),
                dto.getScheduledAt()
        ));

        return ServiceScheduleResponseDto.from(booking.schedule());
    }
}
