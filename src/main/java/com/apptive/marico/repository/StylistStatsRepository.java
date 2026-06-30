package com.apptive.marico.repository;

import com.apptive.marico.entity.StylistStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StylistStatsRepository extends JpaRepository<StylistStats, Long> {
    Page<StylistStats> findByTotalRevenueGreaterThanEqual(long minRevenue, Pageable pageable);
}
