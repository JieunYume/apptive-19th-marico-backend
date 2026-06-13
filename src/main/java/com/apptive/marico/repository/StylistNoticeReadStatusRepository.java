package com.apptive.marico.repository;

import com.apptive.marico.entity.StylistNoticeReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StylistNoticeReadStatusRepository extends JpaRepository<StylistNoticeReadStatus, Long> {

    @Query("SELECT n FROM StylistNoticeReadStatus n JOIN FETCH n.notice WHERE n.stylist.userId = :userId")
    List<StylistNoticeReadStatus> findByStylistUserId(String userId);

    @Modifying
    @Query("DELETE FROM StylistNoticeReadStatus n WHERE n.notice.id = :noticeId")
    void deleteByNoticeId(Long noticeId);
}
