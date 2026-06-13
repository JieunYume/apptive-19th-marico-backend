package com.apptive.marico.repository;

import com.apptive.marico.entity.MemberNoticeReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberNoticeReadStatusRepository extends JpaRepository<MemberNoticeReadStatus, Long> {

    @Query("SELECT n FROM MemberNoticeReadStatus n JOIN FETCH n.notice WHERE n.member.userId = :userId")
    List<MemberNoticeReadStatus> findByMemberUserId(String userId);

    @Modifying
    @Query("DELETE FROM MemberNoticeReadStatus n WHERE n.notice.id = :noticeId")
    void deleteByNoticeId(Long noticeId);
}
