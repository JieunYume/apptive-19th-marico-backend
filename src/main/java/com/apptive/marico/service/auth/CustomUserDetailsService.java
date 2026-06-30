package com.apptive.marico.service.auth;

import com.apptive.marico.dto.LoginDto;
import com.apptive.marico.dto.mypage.member.MemberRequestDto;
import com.apptive.marico.dto.mypage.member.MemberResponseDto;
import com.apptive.marico.dto.stylist.StylistRequestDto;
import com.apptive.marico.dto.token.TokenDto;
import com.apptive.marico.dto.token.TokenRequestDto;
import com.apptive.marico.dto.token.TokenResponseDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.entity.token.RefreshToken;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.jwt.TokenProvider;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.RefreshTokenRepository;
import com.apptive.marico.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.apptive.marico.exception.ErrorCode.*;

/**
 * username을 가지고 사용자 정보를 조회하고 session에 저장될 사용자 주체 정보인 UserDetails를 반환하는 Interface
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final StylistRepository stylistRepository;

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        Optional<Member> memberOptional = memberRepository.findByUserId(userId);
        Optional<Stylist> stylistOptional = stylistRepository.findByUserId(userId);
        if (memberOptional.isPresent()) {
            return createUserDetails(memberOptional.get());
        } else if (stylistOptional.isPresent()) {
            return createUserDetails(stylistOptional.get());
        } else {
            throw new CustomException(ID_OR_PASSWORD_NOT_MATCH);
        }
    }

    @Transactional
    public TokenDto login(LoginDto loginDto) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        isUserExist(loginDto.getUserId());
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 토큰 발급
        return tokenDto;
    }

    public TokenDto refreshToken(String accessToken,String refreshToken) {
        if(!tokenProvider.validateTokenExceptExpiration(accessToken)) throw new CustomException(VALID_ACCESS_TOKEN);
        if (tokenProvider.validateToken(refreshToken)) {
            Authentication authentication = tokenProvider.getAuthentication(refreshToken);
            return tokenProvider.reissueTokenDto(authentication, refreshToken);
        }
        else {
            throw new CustomException(TOKEN_NOT_FOUND);
        }
    }

    public void isUserExist(String userId) {
        Optional<Member> member = memberRepository.findByUserId(userId);
        Optional<Stylist> stylist = stylistRepository.findByUserId(userId);

        if(member.isEmpty() && stylist.isEmpty()) {
            throw new CustomException(ID_OR_PASSWORD_NOT_MATCH);
        }
    }

    @Transactional
    public void logout(String accessToken) {
        // 로그아웃하려는 사용자의 정보를 가져옴
        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // 저장소에서 해당 사용자의 refresh token 삭제
        refreshTokenRepository.deleteByKey(authentication.getName());
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

    public void checkAvailability(MemberRequestDto memberRequestDto) {
        checkEmailAvailability(memberRequestDto.getEmail());
        checkUserIdAvailability(memberRequestDto.getUserId());
        checkPasswordAvailability(memberRequestDto.getPassword());
        checkNicknameAvailability(memberRequestDto.getNickname());
    }

    public void checkAvailability(StylistRequestDto stylistRequestDto) {
        checkEmailAvailability(stylistRequestDto.getEmail());
        checkUserIdAvailability(stylistRequestDto.getUserId());
        checkPasswordAvailability(stylistRequestDto.getPassword());
        checkNicknameAvailability(stylistRequestDto.getNickname());
    }

    private void checkEmailAvailability(String email) {
        if (stylistRepository.existsByEmail(email) || memberRepository.existsByEmail(email)) {
            throw new CustomException(ALREADY_SAVED_EMAIL);
        }
    }

    private void checkUserIdAvailability(String userId) {
        if (memberRepository.existsByUserId(userId) || stylistRepository.existsByUserId(userId)) {
            throw new CustomException(ALREADY_SAVED_ID);
        }

        Pattern userIdPattern = Pattern.compile("^[a-z0-9]{6,20}$");
        Matcher userIdMatcher = userIdPattern.matcher(userId);
        if (!userIdMatcher.matches()) {
            throw new CustomException(INVALID_ID);
        }
    }

    public void checkPasswordAvailability(String password) {
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$");
        Matcher passwordMatcher = passwordPattern.matcher(password);
        if (!passwordMatcher.matches()) {
            throw new CustomException(INVALID_PASSWORD);
        }
    }

    private void checkNicknameAvailability(String nickname) {
        if (memberRepository.existsByNickname(nickname) || stylistRepository.existsByNickname(nickname)) {
            throw new CustomException(ALREADY_SAVED_NICKNAME);
        }

        Pattern nicknamePattern = Pattern.compile("^[가-힣a-z0-9]{2,10}$");
        Matcher nicknameMatcher = nicknamePattern.matcher(nickname);
        if (!nicknameMatcher.matches()) {
            throw new CustomException(INVALID_NICKNAME);
        }
    }
}
