package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StylistRepository extends JpaRepository<Stylist,Long> {
    Optional<Stylist> findByUserId(String userId);
    Optional<Stylist> findByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}
