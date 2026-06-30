-- ============================================================
-- Marico Mock Data: 리뷰 샘플 데이터
-- review_count 반정규화/쿼리 테스트용 소규모 데이터
--   - 회원 200명
--   - 서비스 5,000개 (mock-data-500k-stylists-realistic.sql 로 만든
--     realbulk 스타일리스트 중 stylist_id 가 가장 작은 5,000명에게 1개씩)
--   - 리뷰 8,000개 (서비스당 평균 1.6건, 일부 서비스는 0건이 되도록 분산)
-- 실행 전제: mock-data-500k-stylists-realistic.sql 먼저 실행
-- 회원 비밀번호: "secret"
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET autocommit         = 0;

-- ============================================================
-- 1. 회원 200명 삽입 (10 x 20 = 200)
-- ============================================================
INSERT INTO members (
    name, email, user_id, password, nickname,
    gender, birth_date, city, state, profile_image,
    height, weight, enabled
)
SELECT
    CONCAT('리뷰회원', LPAD(n, 3, '0')),
    CONCAT('reviewmember', n, '@test.com'),
    CONCAT('reviewmember', LPAD(n, 3, '0')),
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    CONCAT('리뷰어', n),
    IF(MOD(n, 2) = 0, 'M', 'F'),
    DATE_ADD('1990-01-01', INTERVAL (n - 1) * 37 DAY),
    ELT(MOD(n - 1, 8) + 1, '서울', '부산', '대구', '인천', '광주', '대전', '수원', '창원'),
    ELT(MOD(n - 1, 8) + 1, '강남구', '해운대구', '수성구', '연수구', '서구', '유성구', '팔달구', '성산구'),
    NULL,
    CAST(150 + MOD(n, 30) AS CHAR),
    CAST(45  + MOD(n, 35) AS CHAR),
    1
FROM (
    SELECT (a.n * 20 + b.n) + 1 AS n
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
         UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
         UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19) b
) nums;

SET @member_min_id = LAST_INSERT_ID();

INSERT INTO members_roles (member_id, role_id)
SELECT m.member_id, r.role_id
FROM members m
JOIN roles r ON r.name = 'ROLE_MEMBER'
WHERE m.member_id BETWEEN @member_min_id AND @member_min_id + 199;

COMMIT;

-- ============================================================
-- 2. 서비스 5,000개 삽입
--    realbulk 스타일리스트 중 stylist_id가 가장 작은 5,000명에게 1개씩 부여
--    (stylist_id 오름차순 5,000명 = city/state 가중 패턴이 50바퀴 반복되므로
--     전체 데이터셋과 비슷한 지역 분포를 그대로 가짐)
-- ============================================================
SET @stylist_min_id = (SELECT MIN(stylist_id) FROM stylists WHERE email LIKE 'realbulk%');

INSERT INTO services (service_name, service_description, price, stylist_id)
SELECT
    CONCAT(s.stage_name, ' 코디 서비스'),
    '퍼스널 컬러 진단부터 코디 제안까지 진행하는 스타일링 서비스입니다.',
    ELT(MOD(s.stylist_id - @stylist_min_id, 5) + 1, 50000, 80000, 100000, 150000, 200000),
    s.stylist_id
FROM stylists s
WHERE s.stylist_id BETWEEN @stylist_min_id AND @stylist_min_id + 4999;

SET @service_min_id = LAST_INSERT_ID();

COMMIT;

-- ============================================================
-- 3. 리뷰 8,000개 삽입
--    서비스 5,000개에 평균 1.6건씩 분산 배정 (일부 서비스는 0건)
--    rating은 4~5점 비중이 높게(만족도 높은 후기가 많은 실제 분포 흉내)
-- ============================================================
INSERT INTO reviews (member_id, service_id, rating, content, created_at)
SELECT
    @member_min_id  + MOD(n * 7, 200),
    @service_min_id + MOD(n - 1, 5000),
    ELT(MOD(n, 5) + 1, 5, 5, 4, 4, 3),
    ELT(MOD(n, 6) + 1,
        '스타일링 정말 만족스러웠어요!',
        '친절하게 상담해주셔서 좋았습니다.',
        '제 체형에 잘 맞는 코디를 제안해주셨어요.',
        '다음에도 또 이용하고 싶습니다.',
        '트렌디한 스타일링 감사합니다.',
        '가격 대비 만족도가 높았어요.'),
    DATE_SUB(NOW(), INTERVAL MOD(n, 365) DAY)
FROM (
    SELECT (a.n + b.n * 10 + c.n * 100 + d.n * 1000) + 1 AS n
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
) nums;

COMMIT;

-- ============================================================
-- 4. stylists.review_count 반정규화 컬럼 추가 + 백필
--    services/reviews 가 없는 나머지 스타일리스트는 LEFT JOIN으로 0 처리
-- ============================================================
ALTER TABLE stylists ADD COLUMN review_count INT NOT NULL DEFAULT 0;

SET SQL_SAFE_UPDATES = 0;  -- WHERE 없는(JOIN 조건만 있는) UPDATE 허용

UPDATE stylists s
LEFT JOIN (
    SELECT svc.stylist_id, COUNT(r.review_id) AS cnt
    FROM services svc
    LEFT JOIN reviews r ON r.service_id = svc.service_id
    GROUP BY svc.stylist_id
) rc ON rc.stylist_id = s.stylist_id
SET s.review_count = COALESCE(rc.cnt, 0);

SET SQL_SAFE_UPDATES = 1;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
SET autocommit         = 1;

-- ============================================================
-- 검증 쿼리
-- ============================================================
-- SELECT review_count, COUNT(*) FROM stylists GROUP BY review_count ORDER BY review_count DESC LIMIT 10;
-- SELECT stylist_id, review_count FROM stylists WHERE review_count > 0 ORDER BY review_count DESC LIMIT 10;

-- ============================================================
-- 검증 쿼리
-- ============================================================
-- SELECT COUNT(*) FROM members WHERE email LIKE 'reviewmember%';   -- 200
-- SELECT COUNT(*) FROM services WHERE stylist_id BETWEEN @stylist_min_id AND @stylist_min_id + 4999; -- 5000
-- SELECT COUNT(*) FROM reviews;                                    -- 8000
-- SELECT svc.stylist_id, COUNT(r.review_id) FROM services svc LEFT JOIN reviews r ON r.service_id = svc.service_id GROUP BY svc.stylist_id ORDER BY 2 DESC LIMIT 10;
