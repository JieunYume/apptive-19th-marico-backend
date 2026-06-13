package com.apptive.marico.service;

import com.apptive.marico.dto.RecommendStylistDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Style;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.exception.ErrorCode;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.apptive.marico.exception.ErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberHomeService {
    private final MemberRepository memberRepository;
    private final StylistRepository stylistRepository;

    public List<RecommendStylistDto> recommendStylist(String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        // 모든 스타일리스트를 가져온다.
        List<Stylist> allStylists = stylistRepository.findAll();
        if(allStylists.isEmpty()) return List.of();

        // 각 스타일리스트에 대해 점수를 계산하고, 이를 기준으로 정렬한다.
        List<Stylist> sortedStylists = allStylists.stream()
                .sorted(Comparator.comparing(stylist -> -calculateScore(member, stylist)))
                .collect(Collectors.toList());
        System.out.println(sortedStylists.get(0) + ", " + sortedStylists.get(1));

        // 추천 스타일리스트가 10명이 넘을 경우, 리스트 크기 조정
        if(sortedStylists.size() > 10) {
            sortedStylists = sortedStylists.subList(0, 10);
        }

        // 추천 스타일리스트 정보를 RecommendStylistDto에 담아 반환
        List<RecommendStylistDto> recommendStylistDtos = sortedStylists.stream()
                .map(stylist -> new RecommendStylistDto(stylist.getId(), stylist.getProfileImage(), stylist.getOneLineIntroduction(), stylist.getStageName()))
                .collect(Collectors.toList());

        return recommendStylistDtos;
    }

    private double calculateScore(Member member, Stylist stylist) {
        double score = 0;

        // 선호 스타일이 일치하는 경우
        for (Style preferredStyle : member.getPreferredStyles()) {
            if (stylist.getStyles().contains(preferredStyle)) {
                score += 10;
            }
        }

        // 도시와 상태가 모두 일치하는 경우
        if(Objects.equals(stylist.getCity(), member.getCity()) && Objects.equals(stylist.getState(), member.getState())) {
            score += 5;
        }
        // 도시만 일치하는 경우
        else if (Objects.equals(stylist.getCity(), member.getCity())) {
            score += 2.5;
        }
        return score;
    }
}
