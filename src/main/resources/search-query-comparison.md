# 스타일리스트 검색 쿼리 구조 비교

## 구조 1 — 실시간 집계 (배치 없음)

매 검색 요청마다 `likes`, `orders`, `style` 테이블을 직접 집계한다.

```sql
SELECT
    s.stylist_id,
    s.profile_image,
    s.stage_name,
    s.one_line_introduction,
    s.city,
    s.state,
    s.gender,
    GROUP_CONCAT(DISTINCT st.category ORDER BY st.category SEPARATOR ',') AS style_categories,
    COALESCE(l.like_count,    0) * 1 +
    COALESCE(ord.order_count, 0) * 5                                       AS score
FROM stylists s
LEFT JOIN style st ON st.stylist_id = s.stylist_id
LEFT JOIN (
    SELECT stylist_id, COUNT(*) AS like_count
    FROM likes
    GROUP BY stylist_id
) l ON l.stylist_id = s.stylist_id
LEFT JOIN (
    SELECT svc.stylist_id, COUNT(DISTINCT o.order_id) AS order_count
    FROM services svc
    JOIN order_services os ON os.service_id = svc.service_id
    JOIN orders o          ON o.order_id    = os.order_id
    WHERE o.status = 'PAID'
    GROUP BY svc.stylist_id
) ord ON ord.stylist_id = s.stylist_id
WHERE s.enabled = true
  AND (st.category = '미니멀')
  AND (s.city      = '부산')
  AND (s.state     = '해운대구')
  AND (s.gender    = 'F')
GROUP BY
    s.stylist_id, s.profile_image, s.stage_name,
    s.one_line_introduction, s.city, s.state, s.gender,
    l.like_count, ord.order_count
ORDER BY score DESC;
```

| 항목 | 내용 |
|---|---|
| 테이블 접근 | `stylists`, `style`, `likes`, `services`, `order_services`, `orders` |
| 병목 | 매 요청마다 `likes`, `orders` 전체 COUNT 집계 |
| 측정 응답 시간 | **3,300ms** |

---

## 구조 2 — 배치 사전 계산 (현재 적용)

`likes`와 `orders` 기반 인기도 점수를 3분마다 사전 계산해 `stylist_search_cache`에 저장한다.  
검색은 이 테이블 하나만 읽는다.

### 배치 쿼리 (3분마다 실행)

```sql
INSERT INTO stylist_search_cache (
    stylist_id, profile_image, stage_name, one_line_introduction,
    city, state, gender, style_categories,
    like_count, order_count, score, updated_at
)
SELECT
    s.stylist_id,
    s.profile_image,
    s.stage_name,
    s.one_line_introduction,
    s.city,
    s.state,
    s.gender,
    GROUP_CONCAT(DISTINCT st.category ORDER BY st.category SEPARATOR ','),
    COALESCE(l.like_count,    0),
    COALESCE(ord.order_count, 0),
    COALESCE(l.like_count,    0) * 1 +
    COALESCE(ord.order_count, 0) * 5,
    NOW()
FROM stylists s
LEFT JOIN style st ON st.stylist_id = s.stylist_id
LEFT JOIN (
    SELECT stylist_id, COUNT(*) AS like_count
    FROM likes
    GROUP BY stylist_id
) l ON l.stylist_id = s.stylist_id
LEFT JOIN (
    SELECT svc.stylist_id, COUNT(DISTINCT o.order_id) AS order_count
    FROM services svc
    JOIN order_services os ON os.service_id = svc.service_id
    JOIN orders o          ON o.order_id    = os.order_id
    WHERE o.status = 'PAID'
    GROUP BY svc.stylist_id
) ord ON ord.stylist_id = s.stylist_id
WHERE s.enabled = true
GROUP BY
    s.stylist_id, s.profile_image, s.stage_name, s.one_line_introduction,
    s.city, s.state, s.gender, l.like_count, ord.order_count
ON DUPLICATE KEY UPDATE
    profile_image         = VALUES(profile_image),
    stage_name            = VALUES(stage_name),
    one_line_introduction = VALUES(one_line_introduction),
    city                  = VALUES(city),
    state                 = VALUES(state),
    gender                = VALUES(gender),
    style_categories      = VALUES(style_categories),
    like_count            = VALUES(like_count),
    order_count           = VALUES(order_count),
    score                 = VALUES(score),
    updated_at            = VALUES(updated_at);
```

### 검색 쿼리 (API 호출 시)

```sql
SELECT
    stylist_id,
    profile_image,
    stage_name,
    one_line_introduction,
    city,
    state,
    gender,
    style_categories
FROM stylist_search_cache
WHERE (FIND_IN_SET('미니멀', style_categories) > 0)
  AND (city   = '부산')
  AND (state  = '해운대구')
  AND (gender = 'F')
ORDER BY score DESC;
```

| 항목 | 내용 |
|---|---|
| 테이블 접근 | `stylist_search_cache` 단일 테이블 |
| 병목 제거 | JOIN·집계 없음, 사전 계산된 score 컬럼 정렬만 수행 |
| 측정 응답 시간 | **250ms** |

---

## 성능 비교

| 구조 | 응답 시간 | 비고 |
|---|---|---|
| 실시간 집계 | 3,300ms | 스타일리스트 100만 건 기준 |
| 배치 사전 계산 | 250ms | 동일 조건 |
| **개선율** | **약 13배** | |

### 트레이드오프

| | 실시간 집계 | 배치 사전 계산 |
|---|---|---|
| 데이터 최신성 | 항상 최신 | 최대 3분 지연 |
| 검색 응답 속도 | 느림 | 빠름 |
| 구현 복잡도 | 단순 | 배치 스케줄러 필요 |
| 대용량 적합성 | 부적합 | 적합 |
