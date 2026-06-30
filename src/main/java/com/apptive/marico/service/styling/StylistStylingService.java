package com.apptive.marico.service.styling;

import com.apptive.marico.dto.styling.MyClientDto;
import com.apptive.marico.dto.styling.MyClientMemberDto;
import com.apptive.marico.dto.styling.payment.PaymentWaitingDeatilDto;
import com.apptive.marico.dto.styling.payment.PaymentWaitingDto;
import com.apptive.marico.dto.styling.payment.PaymentWaitingMemberDto;
import com.apptive.marico.entity.*;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.apptive.marico.exception.ErrorCode.*;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StylistStylingService {
    private final MemberRepository memberRepository;
    private final StylistRepository stylistRepository;
    private final ServiceMatchingRepository orderServiceRepository;


    public MyClientDto findMyClient(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        List<MyClientMemberDto> myClientDtos = new ArrayList<>();
        for (Service stylistService : stylist.getStylistServices()) {
            List<ServiceMatching> matchings = orderServiceRepository.findByService(stylistService);
            for (ServiceMatching matching : matchings) {
                if (matching.getApprovalStatus().equals("DONE")) {
                    myClientDtos.add(MyClientMemberDto.toDto(matching.getMember()));
                }
            }
        }
        if (myClientDtos.size() == 0) {
            throw new CustomException(NO_MY_CLIENT);
        }
        return MyClientDto.toDto(myClientDtos);
    }

    public PaymentWaitingDto findPaymentWaitingList(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        List<PaymentWaitingMemberDto> paymentWaitingMembers = new ArrayList<>();
        for (Service stylistService : stylist.getStylistServices()) {
            List<ServiceMatching> matchings = orderServiceRepository.findByService(stylistService);
            if (matchings != null) {
                for (ServiceMatching matching : matchings) {
                    if (matching.getApprovalStatus().equals("WAITING")) {
                        Member member = matching.getMember();
                        paymentWaitingMembers.add(PaymentWaitingMemberDto.toDto(matching.getId(), member));
                    }
                }
            }
        }
        return PaymentWaitingDto.toDto(paymentWaitingMembers);
    }

    public PaymentWaitingDeatilDto findPaymentWaitingDetail(String userId, long matchingId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findById(matchingId).orElseThrow(
                () -> new CustomException(STYLIST_MATCHING_NOT_FOUND));
        Member member = matching.getMember();

        Set<String> preferredStyleCategoriesSet = new HashSet<>();
        for (Style style : member.getPreferredStyles()) {
            preferredStyleCategoriesSet.add(style.getCategory());
        }

        List<String> preferredStyleCategories = new ArrayList<>(preferredStyleCategoriesSet);

        return PaymentWaitingDeatilDto.toDto(matching.getId(), matching.getMember(), preferredStyleCategories);
    }

    @Transactional
    public String paymentApproval(String userId, long matchingId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findById(matchingId).orElseThrow(
                () -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        if (matching.getApprovalStatus().equals("WAITING")) {
            matching.approval();
        } else {
            throw new CustomException(STYLIST_MATCHING_NOT_WAITING);
        }
        return "결제 승인이 정상적으로 완료되었습니다.";
    }

    @Transactional
    public String paymentDenial(String userId, long matchingId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findById(matchingId).orElseThrow(
                () -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        if (matching.getApprovalStatus().equals("WAITING")) {
            matching.denial();
        }
        return "결제 거절이 정상적으로 완료되었습니다.";
    }
}
