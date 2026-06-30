-- ============================================================
-- Marico Bulk Mock Data (실제 지역 비율 반영 버전)
-- 스타일리스트 50만 명 삽입
-- 비밀번호: "secret"
-- city/state 분포는 실제 인구 비율을 대략적으로 반영한 가중치 사용
-- 실행 전: mysql -u [user] -p [db] < mock-data-500k-stylists-realistic.sql
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS     = 0;  -- unique 인덱스 검증 비활성화 (삽입 속도 향상)
SET autocommit        = 0;  -- 자동 커밋 비활성화 (트랜잭션 일괄 처리)

-- ============================================================
-- 1. 스타일리스트 50만 명 삽입
--    숫자 생성: a,b,c,d,e(0~9) × f(0~4) → 10^4 * 10 * 5 = 500,000
--
--    bucket (1~100): n 을 100으로 나눈 나머지 + 1
--      → 누적 구간으로 실제 인구 비율과 대략 비슷하게 도시를 배정
--        서울 33, 부산 11, 인천 10, 대구 9, 수원 5, 대전 5, 광주 5,
--        창원 4, 용인 4, 고양 4, 울산 4, 성남 3, 부천 3   (합계 100)
--    district: 도시별 실제 구 목록 중 n 기반으로 순환 선택
-- ============================================================
INSERT INTO stylists (
    name, email, user_id, password, nickname,
    gender, birth_date, city, state,
    profile_image, stage_name, one_line_introduction, stylist_introduction,
    chat_link, bank, account_number, account_holder, enabled
)
SELECT
    -- 이름: 10 성 × 20 이름 조합
    CONCAT(
        ELT(MOD(n - 1, 10) + 1,
            '김', '이', '박', '최', '정', '강', '윤', '임', '오', '한'),
        ELT(MOD((n - 1) DIV 10, 20) + 1,
            '민준', '서연', '지호', '유나', '하늘',
            '도현', '태양', '채원', '지민', '수진',
            '준혁', '하은', '성민', '예진', '재원',
            '나연', '현우', '소희', '동현', '미래')
    ),
    CONCAT('realbulk', n, '@test.com'),
    CONCAT('rbuser', n),
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    CONCAT('스타일러', n),
    IF(MOD(n - 1, 2) = 0, 'M', 'F'),
    MAKEDATE(1975 + MOD(n - 1, 25), MOD(n - 1, 365) + 1),

    -- city: 실제 인구 비율(대략치)로 가중 배정한 12개 도시
    CASE
        WHEN MOD(n - 1, 100) + 1 BETWEEN 1  AND 33 THEN '서울'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 34 AND 44 THEN '부산'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 45 AND 54 THEN '인천'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 55 AND 63 THEN '대구'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 64 AND 68 THEN '수원'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 69 AND 73 THEN '대전'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 74 AND 78 THEN '광주'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 79 AND 82 THEN '창원'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 83 AND 86 THEN '용인'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 87 AND 90 THEN '고양'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 91 AND 94 THEN '울산'
        WHEN MOD(n - 1, 100) + 1 BETWEEN 95 AND 97 THEN '성남'
        ELSE '부천'
    END,

    -- state: 위에서 정해진 도시의 실제 구 목록 중 하나를 순환 배정
    CASE
        WHEN MOD(n - 1, 100) + 1 BETWEEN 1 AND 33 THEN
            ELT(MOD(n - 1, 25) + 1,
                '종로구', '중구', '용산구', '성동구', '광진구', '동대문구', '중랑구',
                '성북구', '강북구', '도봉구', '노원구', '은평구', '서대문구', '마포구',
                '양천구', '강서구', '구로구', '금천구', '영등포구', '동작구', '관악구',
                '서초구', '강남구', '송파구', '강동구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 34 AND 44 THEN
            ELT(MOD(n - 1, 15) + 1,
                '중구', '서구', '동구', '영도구', '부산진구', '동래구', '남구',
                '북구', '해운대구', '사하구', '금정구', '강서구', '연제구', '수영구', '사상구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 45 AND 54 THEN
            ELT(MOD(n - 1, 8) + 1,
                '중구', '동구', '미추홀구', '연수구', '남동구', '부평구', '계양구', '서구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 55 AND 63 THEN
            ELT(MOD(n - 1, 7) + 1,
                '중구', '동구', '서구', '남구', '북구', '수성구', '달서구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 64 AND 68 THEN
            ELT(MOD(n - 1, 4) + 1, '장안구', '권선구', '팔달구', '영통구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 69 AND 73 THEN
            ELT(MOD(n - 1, 5) + 1, '동구', '중구', '서구', '유성구', '대덕구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 74 AND 78 THEN
            ELT(MOD(n - 1, 5) + 1, '동구', '서구', '남구', '북구', '광산구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 79 AND 82 THEN
            ELT(MOD(n - 1, 5) + 1, '의창구', '성산구', '마산합포구', '마산회원구', '진해구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 83 AND 86 THEN
            ELT(MOD(n - 1, 3) + 1, '처인구', '기흥구', '수지구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 87 AND 90 THEN
            ELT(MOD(n - 1, 3) + 1, '덕양구', '일산동구', '일산서구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 91 AND 94 THEN
            ELT(MOD(n - 1, 4) + 1, '중구', '남구', '동구', '북구')
        WHEN MOD(n - 1, 100) + 1 BETWEEN 95 AND 97 THEN
            ELT(MOD(n - 1, 3) + 1, '수정구', '중원구', '분당구')
        ELSE
            ELT(MOD(n - 1, 3) + 1, '원미구', '소사구', '오정구')
    END,

    NULL, -- profile_image

    -- stage_name
    CONCAT(
        ELT(MOD(n - 1, 10) + 1,
            '김', '이', '박', '최', '정', '강', '윤', '임', '오', '한'),
        ELT(MOD((n - 1) DIV 5, 5) + 1, '준', '연', '호', '나', '하'),
        ' 스타일'
    ),

    -- one_line_introduction
    ELT(MOD(n - 1, 8) + 1,
        '당신의 스타일을 완성해드립니다',
        '트렌디한 스타일링으로 새로운 나를 만들어드려요',
        '패션으로 자신감을 찾아드립니다',
        '당신만의 특별한 스타일을 만들어드려요',
        '스타일링으로 일상을 특별하게',
        '최신 트렌드를 반영한 맞춤 스타일링',
        '개성 넘치는 스타일로 주목받으세요',
        '편하면서도 세련된 룩을 제안합니다'),

    '경력 있는 전문 스타일리스트입니다. 퍼스널 컬러 진단부터 코디 컨설팅까지 다양한 서비스를 제공합니다.',

    CONCAT('https://open.kakao.com/realbulk', n),

    ELT(MOD(n - 1, 5) + 1,
        '국민은행', '신한은행', '우리은행', '하나은행', '농협은행'),
    LPAD(n, 12, '0'),
    CONCAT(
        ELT(MOD(n - 1, 10) + 1,
            '김', '이', '박', '최', '정', '강', '윤', '임', '오', '한'),
        ELT(MOD((n - 1) DIV 10, 20) + 1,
            '민준', '서연', '지호', '유나', '하늘',
            '도현', '태양', '채원', '지민', '수진',
            '준혁', '하은', '성민', '예진', '재원',
            '나연', '현우', '소희', '동현', '미래')
    ),
    1

FROM (
    SELECT (a.n + b.n * 10 + c.n * 100 + d.n * 1000 + e.n * 10000 + f.n * 100000) + 1 AS n
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e,
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) f
) nums;

COMMIT;

-- ============================================================
-- 2. ROLE_STYLIST 권한 부여
-- ============================================================
SET @first_id = LAST_INSERT_ID();
SET @last_id  = @first_id + 499999;

INSERT INTO stylists_roles (stylist_id, role_id)
SELECT s.stylist_id, r.role_id
FROM stylists s
JOIN roles r ON r.name = 'ROLE_STYLIST'
WHERE s.stylist_id BETWEEN @first_id AND @last_id;

COMMIT;

-- ============================================================
-- 3. 스타일 삽입 (스타일리스트당 2개, 총 100만 행)
-- ============================================================
INSERT INTO style (image, category, stylist_id)
SELECT
    NULL,
    ELT(MOD(stylist_id - @first_id, 10) + 1,
        '캐주얼', '포멀', '미니멀', '스트릿', '빈티지',
        '스포티', '클래식', '보헤미안', '아방가르드', '레트로'),
    stylist_id
FROM stylists
WHERE stylist_id BETWEEN @first_id AND @last_id;

INSERT INTO style (image, category, stylist_id)
SELECT
    NULL,
    ELT(MOD(stylist_id - @first_id + 5, 10) + 1,
        '캐주얼', '포멀', '미니멀', '스트릿', '빈티지',
        '스포티', '클래식', '보헤미안', '아방가르드', '레트로'),
    stylist_id
FROM stylists
WHERE stylist_id BETWEEN @first_id AND @last_id;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS     = 1;
SET autocommit        = 1;
