package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);

//    Optional<User> findByNicknameAndFindQuesNumAndFindAnswer(String nickname, Integer findQuesNum, String findAnswer);
}
