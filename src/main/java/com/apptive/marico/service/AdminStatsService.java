package com.apptive.marico.service;

import com.apptive.marico.dto.admin.StylistStatsDto;
import com.apptive.marico.repository.StylistStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final StylistStatsRepository stylistStatsRepository;

    public Page<StylistStatsDto> getStylistStats(long minRevenue, Pageable pageable) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("totalRevenue").descending()
        );
        return stylistStatsRepository.findByTotalRevenueGreaterThanEqual(minRevenue, sorted)
                .map(StylistStatsDto::from);
    }
}
