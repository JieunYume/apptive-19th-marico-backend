package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Style;
import com.apptive.marico.entity.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StylistRepository extends JpaRepository<Stylist,Long> {
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

}
