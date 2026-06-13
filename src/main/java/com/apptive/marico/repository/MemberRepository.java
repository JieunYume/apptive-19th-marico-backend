package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Style;
import com.apptive.marico.entity.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByUserId(String userId);
    Optional<Member> findByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.noticeReadStatuses nrs LEFT JOIN FETCH nrs.notice WHERE m.userId = :userId")
    Optional<Member> findByUserIdWithNoticeReadStatus(String userId);


//    Optional<User> findByNicknameAndFindQuesNumAndFindAnswer(String nickname, Integer findQuesNum, String findAnswer);
}
