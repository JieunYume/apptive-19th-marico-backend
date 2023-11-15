package com.apptive.marico.service.auth;

import com.apptive.marico.dto.member.MemberRequestDto;
import com.apptive.marico.dto.member.MemberResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Role;
import com.apptive.marico.entity.token.VerificationToken;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.RoleRepository;
import com.apptive.marico.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.apptive.marico.entity.Role.RoleName.ROLE_MEMBER;
import static com.apptive.marico.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberAuthService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public MemberResponseDto signup(MemberRequestDto memberRequestDto) {
        Role userRole = roleRepository.findByName(ROLE_MEMBER).orElseThrow(
                () -> new CustomException(ROLE_NOT_FOUND));

        // 유효성 검사
        validateUserId(memberRequestDto.getUserId());
        validateEmail(memberRequestDto.getEmail());
        validatePassword(memberRequestDto.getPassword());
        validateNickname(memberRequestDto.getNickname());

        Member member = memberRequestDto.toMember(passwordEncoder);
        member.setRoles(Collections.singleton(userRole));

        return MemberResponseDto.toDto(memberRepository.save(member));
    }

    public void validateUserId(String userId) {
        if (memberRepository.existsByUserId(userId)) {
            throw new CustomException(ALREADY_SAVED_ID);
        }

        Pattern userIdPattern = Pattern.compile("^[a-z0-9]{6,20}$");
        Matcher userIdMatcher = userIdPattern.matcher(userId);
        if (!userIdMatcher.matches()) {
            throw new CustomException(INVALID_ID);
        }
    }

    public void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ALREADY_SAVED_EMAIL);
        }
    }

    public void validatePassword(String password) {
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$");
        Matcher passwordMatcher = passwordPattern.matcher(password);
        if (!passwordMatcher.matches()) {
            throw new CustomException(INVALID_PASSWORD);
        }
    }

    public void validateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ALREADY_SAVED_NICKNAME);
        }

        Pattern nicknamePattern = Pattern.compile("^[가-힣a-z0-9]{2,10}$");
        Matcher nicknameMatcher = nicknamePattern.matcher(nickname);
        if (!nicknameMatcher.matches()) {
            throw new CustomException(INVALID_NICKNAME);
        }
    }

    public String changePassword(Member member, String password, String code) throws Exception{
        VerificationToken verificationToken = verificationTokenRepository.findByVerificationCode(code);
        if (verificationToken == null) return "인증번호가 일치하지 않습니다.";
        if(!verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            return "인증 시간이 초과 되었습니다.";
        }
        if(member == null) throw new Exception("changePassword(),member가 조회되지 않음");
        member.setPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
        return "비밀번호가 변경되었습니다.";
    }

}
