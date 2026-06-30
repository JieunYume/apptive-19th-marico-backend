-- ============================================================
-- Marico Mock Data: 서비스 가격 다양화 + order_services(매칭~결제~승인) 샘플 (수정판)
-- findStylistStats 테스트용
--
-- 이전 버전의 문제:
--   1) services.price가 5개 값만 순환해서 가격이 너무 거칠게 나뉨
--   2) 서비스당 order_services 2건을 n / n+10000 으로 만들었는데
--      상태식이 MOD(n,20)이라 10000=20*500로 나눠지는 바람에
--      두 row가 항상 같은 결제 상태로 묶여버려 매출이 0 또는 price*2 둘 중 하나만 나옴
--   → ORDER BY totalRevenue DESC 했을 때 최고가(200000)*2=400000 티어가 무더기로 쌓여 보임
--
-- 수정:
--   - price를 10단계(30000~300000)로 넓힘
--   - 서비스당 주문 건수를 0~6건으로 가변화 (MOD(service_id,7))
--   - 상태 결정식을 (service_id, attempt) 조합으로 만들어 같은 서비스의 주문끼리도 독립적으로 갈리게 함
--
-- 실행 전제: mock-data-500k-stylists-realistic.sql, mock-data-reviews-sample.sql 먼저 실행
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET autocommit         = 0;
SET SQL_SAFE_UPDATES   = 0;

-- ============================================================
-- 0. 이전 버전(mock_tid_%)으로 만든 order_services는 정리하고 다시 생성
-- ============================================================
DELETE FROM order_services WHERE tid LIKE 'mock_tid_%';

-- ============================================================
-- 1. 서비스 가격 다양화 (기존 5,000개 + 신규 5,000개 모두 10단계로)
-- ============================================================
SET @stylist_min_id = (SELECT MIN(stylist_id) FROM stylists WHERE email LIKE 'realbulk%');

UPDATE services svc
JOIN stylists s ON s.stylist_id = svc.stylist_id
SET svc.price = (MOD(s.stylist_id - @stylist_min_id, 10) + 1) * 30000
WHERE s.stylist_id BETWEEN @stylist_min_id AND @stylist_min_id + 9999;

INSERT INTO services (service_name, service_description, price, stylist_id)
SELECT
    CONCAT(s.stage_name, ' 코디 서비스'),
    '퍼스널 컬러 진단부터 코디 제안까지 진행하는 스타일링 서비스입니다.',
    (MOD(s.stylist_id - @stylist_min_id, 10) + 1) * 30000,
    s.stylist_id
FROM stylists s
WHERE s.stylist_id BETWEEN @stylist_min_id + 5000 AND @stylist_min_id + 9999
  AND NOT EXISTS (SELECT 1 FROM services WHERE stylist_id = s.stylist_id);

COMMIT;

-- ============================================================
-- 2. order_services 생성 — 서비스당 0~6건, 상태는 (service_id, attempt) 독립 조합
-- ============================================================
SET @member_min_id = (SELECT MIN(member_id) FROM members WHERE email LIKE 'reviewmember%');

INSERT INTO order_services (service_id, member_id, price, approval_status, tid, ordered_at, status)
SELECT
    svc.service_id,
    @member_min_id + MOD(svc.service_id * 13 + att.attempt * 29, 200),
    svc.price,
    CASE
        WHEN MOD(svc.service_id * 7 + att.attempt * 13, 23) < 16 THEN 'DONE'     -- 16/23 ≈ 70%
        WHEN MOD(svc.service_id * 7 + att.attempt * 13, 23) < 19 THEN 'WAITING'  -- 3/23  ≈ 13%
        WHEN MOD(svc.service_id * 7 + att.attempt * 13, 23) < 20 THEN 'DENY'     -- 1/23  ≈ 4%
        ELSE 'READY'                                                            -- 3/23  ≈ 13% (미결제)
    END,
    CONCAT('mock_tid_', svc.service_id, '_', att.attempt),
    CASE WHEN MOD(svc.service_id * 7 + att.attempt * 13, 23) < 20
         THEN DATE_SUB(NOW(), INTERVAL MOD(svc.service_id + att.attempt, 365) DAY)
         ELSE NULL END,
    CASE WHEN MOD(svc.service_id * 7 + att.attempt * 13, 23) < 20 THEN 'PAID' ELSE NULL END
FROM (
    SELECT service_id, price, stylist_id, MOD(service_id, 7) AS order_count
    FROM services
    WHERE stylist_id BETWEEN @stylist_min_id AND @stylist_min_id + 9999
) svc
JOIN (
    SELECT 1 AS attempt UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
) att ON att.attempt <= svc.order_count;

COMMIT;

SET SQL_SAFE_UPDATES   = 1;
SET FOREIGN_KEY_CHECKS = 1;
SET autocommit         = 1;

-- ============================================================
-- 검증 쿼리
-- ============================================================
-- SELECT approval_status, status, COUNT(*) FROM order_services GROUP BY approval_status, status;
-- SELECT price, COUNT(*) FROM services WHERE stylist_id BETWEEN @stylist_min_id AND @stylist_min_id+9999 GROUP BY price;
-- SELECT COUNT(*) FROM (
--   SELECT svc.stylist_id, COALESCE(SUM(CASE WHEN os.status='PAID' THEN os.price END),0) AS rev
--   FROM services svc LEFT JOIN order_services os ON os.service_id = svc.service_id
--   GROUP BY svc.stylist_id
-- ) t GROUP BY rev ORDER BY rev DESC LIMIT 20;  -- revenue 값별로 몇 명씩 몰리는지 확인
