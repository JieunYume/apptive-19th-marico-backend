package com.apptive.marico.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StylistStatsBatchService {

    @PersistenceContext
    private EntityManager entityManager;

    // findStylistStats 집계는 stylists/services/order_services 전체를 매번 GROUP BY 하는 비싼 쿼리라
    // 3분마다 미리 계산해서 stylist_stats에 적재해두고, API는 이 결과만 읽게 함
    @Scheduled(fixedRate = 180_000)
    @Transactional
    public void refreshStylistStats() {
        int updated = entityManager.createNativeQuery("""
                INSERT INTO stylist_stats (
                    stylist_id, stage_name, profile_image,
                    total_order_count, total_revenue, total_client_count,
                    last_matching_date, updated_at
                )
                SELECT
                    s.stylist_id,
                    s.stage_name,
                    s.profile_image,
                    COUNT(DISTINCT CASE WHEN os.status = 'PAID' THEN os.order_service_id END),
                    COALESCE(SUM(CASE WHEN os.status = 'PAID' THEN os.price END), 0),
                    COUNT(DISTINCT CASE WHEN os.approval_status = 'DONE' THEN os.order_service_id END),
                    MAX(CASE WHEN os.status = 'PAID' THEN os.ordered_at END),
                    NOW()
                FROM stylists s
                LEFT JOIN services svc      ON svc.stylist_id = s.stylist_id
                LEFT JOIN order_services os ON os.service_id  = svc.service_id
                GROUP BY s.stylist_id, s.stage_name, s.profile_image
                ON DUPLICATE KEY UPDATE
                    stage_name         = VALUES(stage_name),
                    profile_image      = VALUES(profile_image),
                    total_order_count  = VALUES(total_order_count),
                    total_revenue      = VALUES(total_revenue),
                    total_client_count = VALUES(total_client_count),
                    last_matching_date = VALUES(last_matching_date),
                    updated_at         = VALUES(updated_at)
                """).executeUpdate();

        log.info("stylist_stats 배치 갱신 완료: {} rows affected", updated);
    }
}
