package com.apptive.marico.service.auth;

import com.apptive.marico.dto.member.MemberResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * username을 가지고 사용자 정보를 조회하고 session에 저장될 사용자 주체 정보인 UserDetails를 반환하는 Interface
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final StylistRepository stylistRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<Member> memberOptional = memberRepository.findByUserId(userId);
        Optional<Stylist> stylistOptional = stylistRepository.findByUserId(userId);
        if (memberOptional.isPresent()) {
            return createUserDetails(memberOptional.get());
        } else if (stylistOptional.isPresent()) {
            return createUserDetails(stylistOptional.get());
        } else {
            throw new UsernameNotFoundException(userId + " -> 데이터베이스에서 찾을 수 없습니다.");
        }
    }

    // DB 에 Member 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(Member member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getAuthorities().toString());
        return new org.springframework.security.core.userdetails.User(
            member.getUsername(),
            member.getPassword(),
            Collections.singleton(grantedAuthority)
        );
    }
    private UserDetails createUserDetails(Stylist stylist) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(stylist.getAuthorities().toString());
        return new org.springframework.security.core.userdetails.User(
                stylist.getUsername(),
                stylist.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
