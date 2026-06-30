package com.apptive.marico.service;

import com.apptive.marico.dto.RecommendStylistDto;
import com.apptive.marico.dto.stylist.StylistSearchResultDto;
import com.apptive.marico.dto.stylistService.StylistFilterDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Style;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.ServiceMatchingRepository;
import com.apptive.marico.repository.ServiceRepository;
import com.apptive.marico.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.apptive.marico.exception.ErrorCode.SERVICE_NOT_FOUND;
import static com.apptive.marico.exception.ErrorCode.USER_NOT_FOUND;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberHomeService {
    private final MemberRepository memberRepository;
    private final StylistRepository stylistRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceMatchingRepository serviceMatchingRepository;

    public List<RecommendStylistDto> recommendStylist(String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        List<Stylist> allStylists = stylistRepository.findAll();
        if (allStylists.isEmpty()) return List.of();

        List<Stylist> sortedStylists = allStylists.stream()
                .sorted(Comparator.comparing(stylist -> -calculateScore(member, stylist)))
                .collect(Collectors.toList());
        System.out.println(sortedStylists.get(0) + ", " + sortedStylists.get(1));

        if (sortedStylists.size() > 10) {
            sortedStylists = sortedStylists.subList(0, 10);
        }

        return sortedStylists.stream()
                .map(stylist -> new RecommendStylistDto(stylist.getId(), stylist.getProfileImage(), stylist.getOneLineIntroduction(), stylist.getStageName()))
                .collect(Collectors.toList());
    }

    public Page<StylistSearchResultDto> searchStylist(StylistFilterDto filter, Pageable pageable) {
        return stylistRepository.searchStylists(
                nullToEmpty(filter.getStyle()),
                nullToEmpty(filter.getCity()),
                nullToEmpty(filter.getState()),
                nullToEmpty(filter.getGender()),
                pageable
        ).map(StylistSearchResultDto::from);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Transactional
    public String applyService(String userId, Long serviceId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(SERVICE_NOT_FOUND));

        serviceMatchingRepository.save(ServiceMatching.builder()
                .member(member)
                .service(service)
                .price(service.getPrice())
                .approvalStatus("READY")
                .build());

        return "서비스 신청이 완료되었습니다.";
    }

    private double calculateScore(Member member, Stylist stylist) {
        double score = 0;

        for (Style preferredStyle : member.getPreferredStyles()) {
            if (stylist.getStyles().contains(preferredStyle)) {
                score += 10;
            }
        }

        if (Objects.equals(stylist.getCity(), member.getCity()) && Objects.equals(stylist.getState(), member.getState())) {
            score += 5;
        } else if (Objects.equals(stylist.getCity(), member.getCity())) {
            score += 2.5;
        }
        return score;
    }
}
