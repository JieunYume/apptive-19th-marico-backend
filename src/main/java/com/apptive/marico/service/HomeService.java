package com.apptive.marico.service;

import com.apptive.marico.dto.AccountDto;
import com.apptive.marico.dto.StylistMatchingDto;
import com.apptive.marico.dto.kakaopay.KakaoPayRedirectResponse;
import com.apptive.marico.dto.stylist.FilterStyleDto;
import com.apptive.marico.dto.stylist.StylistDetailDto;
import com.apptive.marico.dto.stylistService.StylistFilterDto;
import com.apptive.marico.entity.*;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.apptive.marico.exception.ErrorCode.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final StylistRepository stylistRepository;
    private final MemberRepository memberRepository;
    private final ServiceRepository stylistServiceRepository;
    private final ServiceMatchingRepository orderServiceRepository;
    private final StyleRepository styleRepository;
    private final KakaoPayService kakaoPayService;

    public List<FilterStyleDto> filter(StylistFilterDto stylistFilterDto) {
        List<Style> filteredStyle = stylistRepository.findAllWithStyle().stream()
                .filter(stylist ->
                        (Objects.equals(stylistFilterDto.getGender(), "All") || Objects.equals(String.valueOf(stylist.getGender()), stylistFilterDto.getGender())) &&
                                (Objects.equals(stylistFilterDto.getCity(), "All") || Objects.equals(stylist.getCity(), stylistFilterDto.getCity()))
                )
                .flatMap(stylist -> stylist.getStyles().stream())
                .filter(style -> Objects.equals(stylistFilterDto.getStyle(), "All") || Objects.equals(style.getCategory(), stylistFilterDto.getStyle()))
                .toList();

        return filteredStyle.stream().map(FilterStyleDto::toDto).collect(Collectors.toList());

    }

    public StylistDetailDto stylistDetailByStylist(Long stylistId) {
        Stylist stylist = stylistRepository.findByIdWithService(stylistId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return StylistDetailDto.toDto(stylist);
    }

    public StylistDetailDto stylistDetailByStyle(Long styleId) {
        Style style = styleRepository.findById(styleId)
                .orElseThrow(() -> new CustomException(STYLE_NOT_FOUND));
        return StylistDetailDto.toDto(style.getStylist());
    }

    public StylistMatchingDto loadApplication(String userId, Long serviceId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Service stylistService = stylistServiceRepository.findServiceWithStylistById(serviceId)
                .orElseThrow(() -> new CustomException(SERVICE_NOT_FOUND));

        Stylist stylist = stylistService.getStylist();
        return StylistMatchingDto.builder()
                .refundAccount(AccountDto.builder()
                        .bank(member.getBank())
                        .accountHolder(member.getAccountHolder())
                        .accountNumber(member.getAccountNumber())
                        .build())
                .stylistAccount(AccountDto.builder()
                        .bank(stylist.getBank())
                        .accountHolder(stylist.getAccountHolder())
                        .accountNumber(stylist.getAccountNumber())
                        .build())
                .build();
    }

    @Transactional
    public KakaoPayRedirectResponse addApplication(String userId, Long serviceId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Service stylistService = stylistServiceRepository.findServiceWithStylistById(serviceId)
                .orElseThrow(() -> new CustomException(SERVICE_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.save(ServiceMatching.builder()
                .member(member)
                .service(stylistService)
                .price(stylistService.getPrice())
                .build());

        return kakaoPayService.ready(
                matching.getId(),
                userId,
                stylistService.getServiceName(),
                stylistService.getPrice()
        );
    }
}
