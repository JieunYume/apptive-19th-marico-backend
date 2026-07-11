package com.apptive.marico;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.dto.schedule.ServiceScheduleResponseDto;
import com.apptive.marico.service.FakeEmailService;
import com.apptive.marico.service.ServiceScheduleService;
import com.apptive.marico.service.ServiceScheduleService.ScheduleBookingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DB 트랜잭션(ServiceScheduleService)과 알림 발송(이메일 전송)을 분리하는 Facade.
 * 알림 발송은 트랜잭션이 끝난 뒤 이 클래스에서 처리하므로, 알림이 오래 걸려도 DB 커넥션을 붙잡지 않는다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceScheduleFacade {

    private final FakeEmailService smtpEmailService;
    private final ServiceScheduleService serviceScheduleService;

    // 2. 트랜잭션과 메일 전송을 분리한 코드: 트랜잭션 종료 후, 같은 스레드에서 동기적으로 알림을 보낸다.
    public ServiceScheduleResponseDto bookSchedule(String userId, ServiceScheduleRequestDto dto) {
        ScheduleBookingResult result = serviceScheduleService.bookScheduleWithTransaction(userId, dto);

        smtpEmailService.sendScheduleNotification(
                result.stylistEmail(),
                result.memberName(),
                result.serviceName(),
                dto.getScheduledAt()
        );

        return result.response();
    }

    // 3. event로 개선한 코드: 이벤트 발행은 트랜잭션 안(ServiceScheduleService)에서 이미 끝났으므로 결과만 그대로 반환한다.
    public ServiceScheduleResponseDto improvedBookSchedule(String userId, ServiceScheduleRequestDto dto) {
        return serviceScheduleService.improvedBookScheduleWithTransaction(userId, dto);
    }
}
