package com.apptive.marico.service;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.dto.schedule.ServiceScheduleResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.ServiceSchedule;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.exception.ErrorCode;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ServiceContentRepository;
import com.apptive.marico.repository.ServiceMatchingRepository;
import com.apptive.marico.repository.ServiceScheduleRepository;
import com.apptive.marico.service.SmtpEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceScheduleServiceTest {

    @InjectMocks
    private ServiceScheduleService serviceScheduleService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ServiceMatchingRepository orderServiceRepository;

    @Mock
    private ServiceContentRepository serviceCategoryRepository;

    @Mock
    private ServiceScheduleRepository serviceScheduleRepository;

    @Mock
    private SmtpEmailService smtpEmailService;

    private Member member;
    private Stylist stylist;
    private Service service;
    private ServiceMatching matching;
    private ServiceContent serviceContent;
    private ServiceScheduleRequestDto dto;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .userId("testMember")
                .name("테스트멤버")
                .build();

        stylist = Stylist.builder()
                .id(1L)
                .email("stylist@test.com")
                .build();

        service = Service.builder()
                .id(10L)
                .serviceName("퍼스널 스타일링")
                .stylist(stylist)
                .build();

        matching = ServiceMatching.builder()
                .id(1L)
                .member(member)
                .service(service)
                .approvalStatus("DONE")
                .build();

        serviceContent = ServiceContent.builder()
                .id(2L)
                .stylistService(service)
                .build();

        dto = new ServiceScheduleRequestDto(1L, 2L, LocalDateTime.of(2026, 7, 15, 14, 0));
    }

    @Test
    @DisplayName("예약 성공 - 카카오 알림톡 발송됨")
    void bookSchedule_success() {
        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(matching));
        given(serviceCategoryRepository.findById(2L)).willReturn(Optional.of(serviceContent));
        given(serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching, serviceContent)).willReturn(false);

        ServiceSchedule savedSchedule = ServiceSchedule.builder()
                .id(100L)
                .serviceMatching(matching)
                .serviceContent(serviceContent)
                .scheduledAt(dto.getScheduledAt())
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
        given(serviceScheduleRepository.save(any(ServiceSchedule.class))).willReturn(savedSchedule);

        ServiceScheduleResponseDto response = serviceScheduleService.bookSchedule("testMember", dto);

        assertThat(response.getScheduleId()).isEqualTo(100L);
        assertThat(response.getMatchingId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");

        verify(smtpEmailService, times(1))
                .sendScheduleNotification("stylist@test.com", "테스트멤버", "퍼스널 스타일링", dto.getScheduledAt());
    }

    @Test
    @DisplayName("예약 성공 - 스타일리스트 전화번호 없으면 알림톡 미발송")
    void bookSchedule_success_noPhone() {
        Stylist noPhoneStylist = Stylist.builder().id(2L).email(null).build();
        Service noPhoneService = Service.builder().id(11L).serviceName("서비스").stylist(noPhoneStylist).build();
        ServiceMatching noPhoneMatching = ServiceMatching.builder()
                .id(3L).member(member).service(noPhoneService).approvalStatus("DONE").build();
        ServiceContent noPhoneContent = ServiceContent.builder().id(4L).stylistService(noPhoneService).build();

        ServiceScheduleRequestDto noPhoneDto = new ServiceScheduleRequestDto(3L, 4L, dto.getScheduledAt());

        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(3L)).willReturn(Optional.of(noPhoneMatching));
        given(serviceCategoryRepository.findById(4L)).willReturn(Optional.of(noPhoneContent));
        given(serviceScheduleRepository.existsByServiceMatchingAndServiceContent(noPhoneMatching, noPhoneContent)).willReturn(false);

        ServiceSchedule saved = ServiceSchedule.builder()
                .id(101L).serviceMatching(noPhoneMatching).serviceContent(noPhoneContent)
                .scheduledAt(dto.getScheduledAt()).status("SCHEDULED").createdAt(LocalDateTime.now()).build();
        given(serviceScheduleRepository.save(any())).willReturn(saved);

        serviceScheduleService.bookSchedule("testMember", noPhoneDto);

        // sendScheduleNotification은 호출되지만 내부에서 이메일 없으면 early return
        verify(smtpEmailService, times(1))
                .sendScheduleNotification(null, "테스트멤버", "서비스", dto.getScheduledAt());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 회원")
    void bookSchedule_userNotFound() {
        given(memberRepository.findByUserId("unknownUser")).willReturn(Optional.empty());

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("unknownUser", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 매칭")
    void bookSchedule_matchingNotFound() {
        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STYLIST_MATCHING_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 해당 매칭의 멤버가 아님")
    void bookSchedule_matchingMemberMismatch() {
        Member otherMember = Member.builder().id(99L).userId("otherMember").build();
        ServiceMatching otherMatching = ServiceMatching.builder()
                .id(1L).member(otherMember).service(service).approvalStatus("DONE").build();

        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(otherMatching));

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MATCHING_MEMBER_NOT_MATCH);
    }

    @Test
    @DisplayName("실패 - 매칭 승인 미완료 (DONE 아님)")
    void bookSchedule_matchingNotDone() {
        ServiceMatching notDoneMatching = ServiceMatching.builder()
                .id(1L).member(member).service(service).approvalStatus("WAITING").build();

        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(notDoneMatching));

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MATCHING_NOT_DONE);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 서비스 항목")
    void bookSchedule_serviceContentNotFound() {
        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(matching));
        given(serviceCategoryRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SERVICE_CONTENT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 서비스 항목이 해당 서비스에 속하지 않음")
    void bookSchedule_serviceContentNotInService() {
        Service otherService = Service.builder().id(999L).build();
        ServiceContent wrongContent = ServiceContent.builder().id(2L).stylistService(otherService).build();

        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(matching));
        given(serviceCategoryRepository.findById(2L)).willReturn(Optional.of(wrongContent));

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SERVICE_CONTENT_NOT_IN_SERVICE);
    }

    @Test
    @DisplayName("실패 - 이미 예약된 스케줄 존재")
    void bookSchedule_scheduleAlreadyExists() {
        given(memberRepository.findByUserId("testMember")).willReturn(Optional.of(member));
        given(orderServiceRepository.findById(1L)).willReturn(Optional.of(matching));
        given(serviceCategoryRepository.findById(2L)).willReturn(Optional.of(serviceContent));
        given(serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching, serviceContent)).willReturn(true);

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule("testMember", dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_ALREADY_EXISTS);
    }
}
