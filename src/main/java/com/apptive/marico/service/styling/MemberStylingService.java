package com.apptive.marico.service.styling;

import com.apptive.marico.dto.styling.MemberBasicInformationDto;
import com.apptive.marico.dto.styling.PersonalStylistDto;
import com.apptive.marico.entity.*;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.apptive.marico.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberStylingService {
    private final MemberRepository memberRepository;
    private final StyleRepository styleRepository;
    private final MyStyleRepository myStyleRepository;
    private final ReferenceImageRepository referenceImageRepository;
    private final ServiceMatchingRepository orderServiceRepository;

    @Transactional
    public PersonalStylistDto findPersonalStylist(String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = orderServiceRepository.findFirstByMemberAndApprovalStatusOrderByIdDesc(member, "DONE")
                .orElseThrow(() -> new CustomException(NO_PERSONAL_STYLIST));

        return PersonalStylistDto.toDto(matching.getService().getStylist());
    }


    @Transactional
    public Object registerBasicInformation(String userId, MemberBasicInformationDto memberBasicInformationDto, List<String> myStyleUrls, List<String> referenceImagesUrls) {
        System.out.println(memberBasicInformationDto.toString());

        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        member.setHeight(memberBasicInformationDto.getHeight());
        member.setWeight(memberBasicInformationDto.getWeight());

        List<Style> newPreferredStyles = new ArrayList<>();
        for (long preferredStyle : memberBasicInformationDto.getPreferredStyles()) {
            Style style = styleRepository.findById(preferredStyle).orElseThrow(
                    () -> new CustomException(STYLE_NOT_FOUND));
            newPreferredStyles.add(style);
        }
        member.setPreferredStyles(newPreferredStyles);
        member.setCity(memberBasicInformationDto.getCity());
        member.setState(memberBasicInformationDto.getState());
        member.setBodyShapeImages(memberBasicInformationDto.getBodyShapeImage());

        List<MyStyle> newMyStyles = new ArrayList<>();
        for (String myStyleUrl : myStyleUrls) {
            MyStyle myStyle = MyStyle.builder()
                    .imageUrl(myStyleUrl)
                    .member(member)
                    .build();
            MyStyle save = myStyleRepository.save(myStyle);
//            newMyStyles.add(myStyleRepository.save(myStyle));
        }
        member.getMyStyles().clear();
        member.getMyStyles().addAll(newMyStyles);

        List<ReferenceImage> newReferenceImages = new ArrayList<>();
        for (String referenceImageUrl : referenceImagesUrls) {
            ReferenceImage referenceImage = ReferenceImage.builder()
                    .imageUrl(referenceImageUrl)
                    .member(member)
                    .build();
            newReferenceImages.add(referenceImageRepository.save(referenceImage));
        }
//        member.getReferenceImages().clear();
        member.getReferenceImages().addAll(newReferenceImages);

        memberRepository.save(member);

        return "수정이 성공적으로 완료되었습니다.";
    }

    public MemberBasicInformationDto findBasicInformation(String userId) {

        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        List<Long> preferredStyleIds = new ArrayList<>();
        for (Style preferredStyle : member.getPreferredStyles()) {
            preferredStyleIds.add(preferredStyle.getId());
        }

        List<String> myStyleUrls = new ArrayList<>();
        for (MyStyle myStyle : member.getMyStyles()) {
            myStyleUrls.add(myStyle.getImageUrl());
        }

        List<String> referenceImageUrls = new ArrayList<>();
        for (ReferenceImage referenceImage : member.getReferenceImages()) {
            referenceImageUrls.add(referenceImage.getImageUrl());
        }

        return MemberBasicInformationDto.toDto(member, preferredStyleIds, myStyleUrls, referenceImageUrls);
    }
}
