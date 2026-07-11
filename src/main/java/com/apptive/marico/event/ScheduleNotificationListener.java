package com.apptive.marico.event;

import com.apptive.marico.service.FakeEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleNotificationListener {

    private final FakeEmailService emailService;

    // 비동기/이 메소드를 어느 스레드에서 실행할지 결정한다. 이게 없으면 트랜잭션 커밋이 끝나도 같은 스레드가 실행한다.
    @Async("emailExecutor")
    // 현재 실행 중인 트랜잭션의 어느 시점에 실행할지 설정 - 성공적으로 커밋된 이후에 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendStylistNotification(ServiceScheduleBookedEvent event){
        emailService.sendScheduleNotification(
                event.getStylistEmail(),
                event.getMemberName(),
                event.getServiceName(),
                event.getScheduledAt()
        );
    }
}
