package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Style;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.repository.projection.StylistSearchView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StylistRepository extends JpaRepository<Stylist,Long> {
    // 리뷰 생성/삭제 시 review_count를 원자적으로 증감 (read-modify-write로 인한 race condition 방지)
    @Modifying
    @Query("UPDATE Stylist s SET s.reviewCount = s.reviewCount + :delta WHERE s.id = :stylistId")
    void addToReviewCount(@Param("stylistId") Long stylistId, @Param("delta") int delta);

    Optional<Stylist> findByUserId(String userId);
    Optional<Stylist> findByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @Query("SELECT DISTINCT s FROM Stylist s LEFT JOIN FETCH s.styles WHERE s.userId = :userId")
    Optional<Stylist> findByUserIdWithStyle(String userId);

    @Query("SELECT s FROM Stylist s LEFT JOIN FETCH s.styles")
    List<Stylist> findAllWithStyle();
    @Query("SELECT s FROM Stylist s LEFT JOIN FETCH s.noticeReadStatuses nrs LEFT JOIN FETCH nrs.notice WHERE s.userId = :userId")
    Optional<Stylist> findByUserIdWithNoticeReadStatus(String userId);

    @Query("SELECT DISTINCT s FROM Stylist s LEFT JOIN FETCH s.stylistServices WHERE s.id = :stylistId")
    Optional<Stylist> findByIdWithService(Long stylistId);

    // styles 값이 파라미터로 주어진 컬렉션에 포함되어 있는 스타일리스트 엔티티들을 조회한다.
    List<Stylist> findByStylesIn(List<Style> styles);

    List<Stylist> findByStylesInAndCityNotOrStateNot(List<Style> styles, String city, String state);

    @Query(value = """
            SELECT
                s.stylist_id                                                                AS stylist_id,
                s.profile_image                                                             AS profile_image,
                s.stage_name                                                                AS stage_name,
                s.one_line_introduction                                                     AS one_line_introduction,
                s.city                                                                      AS city,
                s.state                                                                     AS state,
                s.gender                                                                    AS gender,
                (SELECT GROUP_CONCAT(DISTINCT st.category ORDER BY st.category SEPARATOR ',')
                 FROM style st WHERE st.stylist_id = s.stylist_id)                         AS style_categories,
                (SELECT COUNT(r.review_id)
                 FROM services svc
                 LEFT JOIN reviews r ON r.service_id = svc.service_id
                 WHERE svc.stylist_id = s.stylist_id)                                      AS review_count
            FROM stylists s
            WHERE s.enabled = true
              AND (:city   = '' OR s.city   = :city)
              AND (:state  = '' OR s.state  = :state)
              AND (:gender = '' OR s.gender = :gender)
              AND (:style  = '' OR EXISTS (
                  SELECT 1 FROM style st
                  WHERE st.stylist_id = s.stylist_id AND st.category = :style
              ))
            ORDER BY review_count DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM stylists s
            WHERE s.enabled = true
              AND (:city   = '' OR s.city   = :city)
              AND (:state  = '' OR s.state  = :state)
              AND (:gender = '' OR s.gender = :gender)
              AND (:style  = '' OR EXISTS (
                  SELECT 1 FROM style st
                  WHERE st.stylist_id = s.stylist_id AND st.category = :style
              ))
            """,
            nativeQuery = true)
    Page<StylistSearchView> searchStylists(
            @Param("style") String style,
            @Param("city") String city,
            @Param("state") String state,
            @Param("gender") String gender,
            Pageable pageable
    );

}
