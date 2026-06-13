package com.apptive.marico.service;

import com.apptive.marico.dto.NoticeDto;
import com.apptive.marico.dto.NoticeResponseDtoList;
import com.apptive.marico.entity.*;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberNoticeReadStatusRepository;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.NoticeRepository;
import com.apptive.marico.repository.StylistNoticeReadStatusRepository;
import com.apptive.marico.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.apptive.marico.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final MemberNoticeReadStatusRepository memberNoticeReadStatusRepository;
    private final StylistNoticeReadStatusRepository stylistNoticeReadStatusRepository;
    private final ModelMapper modelMapper;
    private final StylistRepository stylistRepository;

    public NoticeResponseDtoList loadNoticeList(String userId) {
        List<Notice> noticeList = noticeRepository.findAll();
        List<NoticeDto> noticeDtoList = noticeList.stream()
                .map(NoticeDto::toDto)
                .toList();

        Optional<Member> member = memberRepository.findByUserIdWithNoticeReadStatus(userId);
        Optional<Stylist> stylist = stylistRepository.findByUserIdWithNoticeReadStatus(userId);

        List<Long> readList = member.map(m ->
                m.getNoticeReadStatuses().stream()
                        .map(s -> s.getNotice().getId())
                        .toList()
        ).orElseGet(() -> stylist.map(s ->
                s.getNoticeReadStatuses().stream()
                        .map(n -> n.getNotice().getId())
                        .toList()
        ).orElse(null));

        return NoticeResponseDtoList.builder().noticeDtoList(noticeDtoList).readNotice(readList).build();
    }

    public NoticeDto loadNotice(String userId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(NOTICE_NOT_FOUND));

        Optional<Member> member = memberRepository.findByUserIdWithNoticeReadStatus(userId);
        if (member.isPresent()) {
            if (isNoticeUnreadForMember(member.get().getNoticeReadStatuses(), noticeId)) {
                memberNoticeReadStatusRepository.save(
                        MemberNoticeReadStatus.builder()
                                .notice(notice)
                                .member(member.get())
                                .readStatus(true)
                                .build()
                );
            }
            return NoticeDto.toDto(notice);
        }

        Optional<Stylist> stylist = stylistRepository.findByUserIdWithNoticeReadStatus(userId);
        if (stylist.isPresent()) {
            if (isNoticeUnreadForStylist(stylist.get().getNoticeReadStatuses(), noticeId)) {
                stylistNoticeReadStatusRepository.save(
                        StylistNoticeReadStatus.builder()
                                .notice(notice)
                                .stylist(stylist.get())
                                .readStatus(true)
                                .build()
                );
            }
        }

        return NoticeDto.toDto(notice);
    }

    private boolean isNoticeUnreadForMember(List<MemberNoticeReadStatus> statuses, Long noticeId) {
        return statuses.stream().noneMatch(s -> Objects.equals(s.getNotice().getId(), noticeId));
    }

    private boolean isNoticeUnreadForStylist(List<StylistNoticeReadStatus> statuses, Long noticeId) {
        return statuses.stream().noneMatch(s -> Objects.equals(s.getNotice().getId(), noticeId));
    }

    public String addNotice(Principal principal, NoticeDto noticeDto) {
        if (!hasAdminRole(principal)) throw new CustomException(USER_NOT_ADMIN);
        if (memberRepository.findByUserId(principal.getName()).isEmpty()) throw new CustomException(USER_NOT_FOUND);
        noticeRepository.save(Notice.builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .build());
        return "공지사항이 등록되었습니다.";
    }

    private boolean hasAdminRole(Principal principal) {
        Member member = memberRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new CustomException(USER_NOT_ADMIN));
        Collection<? extends GrantedAuthority> authorities = member.getAuthorities();
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public String deleteNotice(Principal principal, Long noticeId) {
        if (!hasAdminRole(principal)) throw new CustomException(USER_NOT_ADMIN);
        if (memberRepository.findByUserId(principal.getName()).isEmpty()) throw new CustomException(USER_NOT_FOUND);
        memberNoticeReadStatusRepository.deleteByNoticeId(noticeId);
        stylistNoticeReadStatusRepository.deleteByNoticeId(noticeId);
        noticeRepository.deleteById(noticeId);
        return "공지사항이 정상적으로 삭제 되었습니다.";
    }
}
