package com.apptive.marico.service;

import com.apptive.marico.ServiceScheduleFacade;
import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ServiceContentRepository;
import com.apptive.marico.repository.ServiceMatchingRepository;
import com.apptive.marico.repository.ServiceRepository;
import com.apptive.marico.repository.ServiceScheduleRepository;
import com.apptive.marico.repository.StylistRepository;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test") // 어떤 스프링 프로필을 활성화할지 지정한다. -> application-test.yml
class ServiceScheduleConcurrencyTest {

    @Autowired
    ServiceScheduleService serviceScheduleService;
    @Autowired
    ServiceScheduleFacade serviceScheduleFacade;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StylistRepository stylistRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ServiceMatchingRepository matchingRepository;
    @Autowired
    ServiceContentRepository contentRepository;
    @Autowired
    ServiceScheduleRepository serviceScheduleRepository;
    @Autowired
    DataSource dataSource; // HikariCP 상태 확인용

    @SpyBean // 진짜 FakeEmailService를 감싼 스파이 -> 스텁을 안 걸면 실제 로직(랜덤 200~500ms sleep + 로그) 그대로 실행
    FakeEmailService smtpEmailService;

    private static final int REQUEST_COUNT = 20;

    // 비동기(emailExecutor)로 실행되는 이메일 발송이 실제로 몇 건 끝났는지 세기 위한 latch
    private CountDownLatch emailLatch;

    @BeforeEach
    void setUpMock() {
        emailLatch = new CountDownLatch(REQUEST_COUNT);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod(); // 실제 FakeEmailService 로직을 그대로 호출
            emailLatch.countDown();
            return result;
        }).when(smtpEmailService).sendScheduleNotification(any(), any(), any(), any());
    }

    @Test
    void before_동기전송_동시요청() throws InterruptedException {
        List<TestCase> cases = prepareTestData("before", REQUEST_COUNT);
        run("BEFORE", cases, (userId, dto) -> serviceScheduleService.bookSchedule(userId, dto));
    }

    @Test
    void after_이벤트비동기_동시요청() throws InterruptedException {
        List<TestCase> cases = prepareTestData("after", REQUEST_COUNT);
        run("AFTER", cases, (userId, dto) -> serviceScheduleFacade.improvedBookSchedule(userId, dto));
    }

    @Test
    void 서로_다른_회원이_같은_시간대를_동시에_예약하면_스타일리스트가_이중예약되는지() throws InterruptedException {
        LocalDateTime sameSlot = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0).withNano(0);

        Stylist stylist = stylistRepository.save(
                Stylist.builder()
                        .name("겹치기스타일리스트")
                        .email("overlap-stylist@test.com")
                        .userId("overlap-stylist")
                        .password("password")
                        .nickname("overlap-stylist-nick")
                        .gender('F')
                        .enabled(true)
                        .build()
        );

        Service service = serviceRepository.save(
                Service.builder()
                        .serviceName("겹치기서비스")
                        .serviceDescription("설명")
                        .price(10000)
                        .stylist(stylist)
                        .build()
        );

        // 두 회원이 예약할 "슬롯"은 같은 서비스의 같은 콘텐츠(카테고리) - 스타일리스트 입장에서는 같은 자리다
        ServiceContent content = contentRepository.save(
                ServiceContent.builder()
                        .stylistService(service)
                        .build()
        );

        Member member1 = memberRepository.save(
                Member.builder()
                        .userId("overlap-user1").name("겹치기유저1")
                        .email("overlap-user1@test.com").password("password")
                        .nickname("overlap-nick1").gender('F').enabled(true)
                        .build()
        );
        Member member2 = memberRepository.save(
                Member.builder()
                        .userId("overlap-user2").name("겹치기유저2")
                        .email("overlap-user2@test.com").password("password")
                        .nickname("overlap-nick2").gender('F').enabled(true)
                        .build()
        );

        // 서로 다른 매칭(=서로 다른 예약 건)이지만, 같은 서비스(=같은 스타일리스트)를 이용한다
        ServiceMatching matching1 = matchingRepository.save(
                ServiceMatching.builder().member(member1).service(service).price(service.getPrice())
                        .approvalStatus("DONE").build()
        );
        ServiceMatching matching2 = matchingRepository.save(
                ServiceMatching.builder().member(member2).service(service).price(service.getPrice())
                        .approvalStatus("DONE").build()
        );

        ServiceScheduleRequestDto dto1 = new ServiceScheduleRequestDto(matching1.getId(), content.getId(), sameSlot);
        ServiceScheduleRequestDto dto2 = new ServiceScheduleRequestDto(matching2.getId(), content.getId(), sameSlot);

        // 두 스레드가 최대한 같은 순간에 출발하도록 준비/출발 신호를 분리한다
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger success = new AtomicInteger();

        pool.submit(() -> {
            ready.countDown();
            awaitLatch(start);
            try {
                serviceScheduleService.bookScheduleWithTransaction(member1.getUserId(), dto1);
                success.incrementAndGet();
            } catch (Exception e) {
                System.out.println("user1 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            } finally {
                done.countDown();
            }
        });
        pool.submit(() -> {
            ready.countDown();
            awaitLatch(start);
            try {
                serviceScheduleService.bookScheduleWithTransaction(member2.getUserId(), dto2);
                success.incrementAndGet();
            } catch (Exception e) {
                System.out.println("user2 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            } finally {
                done.countDown();
            }
        });

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        boolean user1Booked = serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching1, content);
        boolean user2Booked = serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching2, content);

        System.out.printf("[이중예약] 성공:%d user1예약:%b user2예약:%b%n", success.get(), user1Booked, user2Booked);

        // 현재 코드는 (matching, content) 쌍만 중복 체크할 뿐, "같은 서비스의 같은 시간대"는 전혀 체크하지 않는다.
        // 그래서 서로 다른 매칭이면 둘 다 통과해서 스타일리스트가 같은 시간에 이중 예약된다 -> 이 assert는 지금 실패해야 정상이다.
        assertThat(user1Booked && user2Booked)
                .as("같은 스타일리스트의 같은 시간대에 서로 다른 두 회원이 동시에 예약되면 안 된다")
                .isFalse();
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void 커넥션_풀_점유_상황에서_트랜잭션_분리_효과를_비교() throws InterruptedException {
        List<TestCase> beforeCases = prepareTestData("perf-before", REQUEST_COUNT);
        List<TestCase> afterCases = prepareTestData("perf-after", REQUEST_COUNT);

        // 커넥션 풀(max 3)이 두 시나리오 모두에서 동일하게 병목이 되는 조건에서, 같은 일(검증+저장+이메일 발송)을
        // 끝내는 전체 요청 시간을 공정하게 비교한다 (이메일 발송을 빼고 트랜잭션만 재는 건 비교 대상이 다름)
        // 1. 원래 코드: 검증+저장+이메일 발송이 한 트랜잭션 안에서 동기로 다 끝날 때까지 걸리는 시간
        //    -> 이메일 대기(200~500ms) 동안 커넥션을 붙잡고 있어 풀 경합이 심해진다
        double beforeAvgMs = measureAverageMillis("BEFORE", beforeCases,
                (userId, dto) -> serviceScheduleService.bookSchedule(userId, dto));

        // 2. 트랜잭션 분리 코드: DB 트랜잭션(검증+저장)은 짧게 끝내고 커넥션을 반납한 뒤, 그 밖에서 이메일을 발송한다
        //    -> 이메일 발송 자체는 여전히 동기로 걸리지만, 커넥션을 오래 붙잡지 않아 다른 요청들의 대기 시간이 줄어든다
        double afterAvgMs = measureAverageMillis("AFTER", afterCases,
                (userId, dto) -> serviceScheduleFacade.bookSchedule(userId, dto));

        double reductionRate = (beforeAvgMs - afterAvgMs) / beforeAvgMs * 100;
        System.out.printf("[성능비교] BEFORE 평균:%.0fms AFTER(트랜잭션 분리) 평균:%.0fms 단축률:%.1f%%%n",
                beforeAvgMs, afterAvgMs, reductionRate);

        // 커넥션 풀(3개) 경합 + 이메일 동기 대기가 빠지므로, 분리한 쪽이 더 빨라야 한다
        assertThat(afterAvgMs).isLessThan(beforeAvgMs);
    }

    // cases를 동시에 실행하면서, 요청 1건당 걸린 시간(성공/실패 무관)의 평균을 반환한다.
    // 중간에 커넥션 풀 점유 현황을 찍어, 실제로 풀 경합이 걸리는 상황에서 측정됐는지 확인한다.
    private double measureAverageMillis(String label, List<TestCase> cases, BiConsumer<String, ServiceScheduleRequestDto> action)
            throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(cases.size());
        CountDownLatch latch = new CountDownLatch(cases.size());
        AtomicLong totalMillis = new AtomicLong();

        HikariDataSource hikari = (HikariDataSource) dataSource;

        for (TestCase c : cases) {
            pool.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    action.accept(c.userId(), c.dto());
                } catch (Exception e) {
                    // 실패도 "요청이 끝나기까지 걸린 시간"이므로 측정에 포함한다 (예: 커넥션 획득 타임아웃 대기)
                } finally {
                    totalMillis.addAndGet(System.currentTimeMillis() - start);
                    latch.countDown();
                }
            });
        }

        // 커넥션 풀이 실제로 꽉 차서 대기가 걸리고 있는지 중간에 한번 찍어본다
        Thread.sleep(200);
        HikariPoolMXBean pmx = hikari.getHikariPoolMXBean();
        System.out.printf("[%s] 진행 중 - active:%d idle:%d waiting:%d%n",
                label, pmx.getActiveConnections(), pmx.getIdleConnections(), pmx.getThreadsAwaitingConnection());

        latch.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        return totalMillis.get() / (double) cases.size();
    }

    @Test
    void 이벤트_기반_이메일_발송이_성공적으로_이뤄지는지() {
        TestCase testCase = prepareTestData("event-success", 1).get(0);

        serviceScheduleFacade.improvedBookSchedule(testCase.userId(), testCase.dto());

        ServiceMatching matching = matchingRepository.findById(testCase.dto().getMatchingId()).orElseThrow();
        // service/stylist는 지연 로딩이라 findById만으로는 세션이 끝난 뒤 접근 시 LazyInitializationException 발생 -> stylist까지 fetch join으로 조회
        Service serviceWithStylist = serviceRepository.findServiceWithStylistById(matching.getService().getId()).orElseThrow();
        String expectedEmail = serviceWithStylist.getStylist().getEmail();
        String expectedServiceName = serviceWithStylist.getServiceName();
        String expectedMemberName = memberRepository.findByUserId(testCase.userId()).orElseThrow().getName();

        // AFTER_COMMIT + @Async라서 리스너가 실행될 때까지 폴링하며 기다린 뒤 호출 여부/인자를 검증
        verify(smtpEmailService, timeout(3000))
                .sendScheduleNotification(expectedEmail, expectedMemberName, expectedServiceName, testCase.dto().getScheduledAt());
    }

    @Test
    void 메일_발송에_실패해도_트랜잭션은_정상적으로_커밋되는지() {
        TestCase testCase = prepareTestData("event-fail", 1).get(0);

        // 이 테스트에서만 이메일 발송이 실패하도록 재스텁
        doThrow(new RuntimeException("SMTP 서버 장애"))
                .when(smtpEmailService).sendScheduleNotification(any(), any(), any(), any());

        // 비동기 리스너 안에서 예외가 나도 호출 스레드(테스트 스레드)로는 전파되지 않아야 한다
        serviceScheduleFacade.improvedBookSchedule(testCase.userId(), testCase.dto());

        // 이메일 발송이 실제로 시도(그리고 실패)될 때까지 대기
        verify(smtpEmailService, timeout(3000))
                .sendScheduleNotification(any(), any(), any(), any());

        ServiceMatching matching = matchingRepository.findById(testCase.dto().getMatchingId()).orElseThrow();
        ServiceContent content = contentRepository.findById(testCase.dto().getServiceContentId()).orElseThrow();

        // 커밋은 트랜잭션 종료 시점에 이미 끝나 있으므로, 이후 이메일 발송 실패와 무관하게 유지되어야 한다
        assertThat(serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching, content)).isTrue();
    }

    @Test
    void 동기_메일_발송_실패시_bookSchedule_트랜잭션도_롤백되는지() {
        TestCase testCase = prepareTestData("sync-fail", 1).get(0);

        // bookSchedule은 트랜잭션 안에서 이메일을 동기 호출하므로, 발송 실패가 곧 메서드 실패로 이어져야 한다
        doThrow(new RuntimeException("SMTP 서버 장애"))
                .when(smtpEmailService).sendScheduleNotification(any(), any(), any(), any());

        assertThatThrownBy(() -> serviceScheduleService.bookSchedule(testCase.userId(), testCase.dto()))
                .isInstanceOf(RuntimeException.class);

        ServiceMatching matching = matchingRepository.findById(testCase.dto().getMatchingId()).orElseThrow();
        ServiceContent content = contentRepository.findById(testCase.dto().getServiceContentId()).orElseThrow();

        // @Transactional의 기본 롤백 규칙(RuntimeException)에 걸려, 이미 실행됐던 스케줄 저장까지 통째로 롤백되어야 한다
        assertThat(serviceScheduleRepository.existsByServiceMatchingAndServiceContent(matching, content)).isFalse();
    }

    private void run(String label, List<TestCase> cases, BiConsumer<String, ServiceScheduleRequestDto> action)
            throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        HikariDataSource hikari = (HikariDataSource) dataSource;

        long start = System.currentTimeMillis();
        for (TestCase c : cases) {
            pool.submit(() -> {
                try {
                    action.accept(c.userId(), c.dto());
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    System.out.println("실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 커넥션 풀 사용 현황을 중간에 한번 찍어보기
        Thread.sleep(200);
        HikariPoolMXBean pmx = hikari.getHikariPoolMXBean();
        System.out.printf("[%s] 진행 중 - active:%d idle:%d waiting:%d%n",
                label, pmx.getActiveConnections(), pmx.getIdleConnections(), pmx.getThreadsAwaitingConnection());

        latch.await(30, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("[%s] 성공:%d 실패:%d 소요시간:%dms%n",
                label, success.get(), fail.get(), elapsed);
        pool.shutdown();

        // 성공한 예약 건수만큼 이메일이 (비동기든 동기든) 실제로 다 발송될 때까지 대기
        boolean allEmailsSent = emailLatch.await(10, TimeUnit.SECONDS);
        long emailElapsed = System.currentTimeMillis() - start;
        System.out.printf("[%s] 이메일 발송 완료:%b (미완료 %d건) 총 소요시간:%dms%n",
                label, allEmailsSent, emailLatch.getCount(), emailElapsed);
    }

    private record TestCase(String userId, ServiceScheduleRequestDto dto) {}

    private List<TestCase> prepareTestData(String prefix, int count) {
        List<TestCase> result = new ArrayList<>();

        Stylist stylist = stylistRepository.save(
                Stylist.builder()
                        .name("테스트스타일리스트-" + prefix)
                        .email(prefix + "-stylist@test.com")
                        .userId(prefix + "-stylist")
                        .password("password")
                        .nickname(prefix + "-stylist-nick")
                        .gender('F')
                        .enabled(true)
                        .build()
        );

        Service service = serviceRepository.save(
                Service.builder()
                        .serviceName("테스트서비스-" + prefix)
                        .serviceDescription("설명")
                        .price(10000)
                        .stylist(stylist)
                        .build()
        );

        for (int i = 0; i < count; i++) {
            Member member = memberRepository.save(
                    Member.builder()
                            .userId(prefix + "-user" + i)
                            .name("테스트유저" + i)
                            .email(prefix + "-user" + i + "@test.com")
                            .password("password")
                            .nickname(prefix + "-nick" + i)
                            .gender('F')
                            .enabled(true)
                            .build()
            );

            ServiceMatching matching = matchingRepository.save(
                    ServiceMatching.builder()
                            .member(member)
                            .service(service)
                            .price(service.getPrice())
                            .approvalStatus("DONE")
                            .build()
            );

            ServiceContent content = contentRepository.save(
                    ServiceContent.builder()
                            .stylistService(matching.getService())
                            .build()
            );

            ServiceScheduleRequestDto dto = new ServiceScheduleRequestDto(
                    matching.getId(),
                    content.getId(),
                    LocalDateTime.now().plusDays(1)
            );

            result.add(new TestCase(member.getUserId(), dto));
        }
        return result;
    }
}