package com.apptive.marico.service.auth;

import com.apptive.marico.dto.LoginDto;
import com.apptive.marico.dto.stylist.StylistRequestDto;
import com.apptive.marico.dto.stylist.StylistResponseDto;
import com.apptive.marico.dto.token.TokenResponseDto;
import com.apptive.marico.entity.Role;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.entity.token.RefreshToken;
import com.apptive.marico.entity.token.VerificationToken;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.jwt.TokenProvider;
import com.apptive.marico.repository.RefreshTokenRepository;
import com.apptive.marico.repository.RoleRepository;
import com.apptive.marico.repository.StylistRepository;
import com.apptive.marico.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.apptive.marico.entity.Role.RoleName.ROLE_STYLIST;
import static com.apptive.marico.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class StylistAuthService {
    private final StylistRepository stylistRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public StylistResponseDto signup(StylistRequestDto stylistRequestDto) {
        Role userRole = roleRepository.findByName(ROLE_STYLIST).orElseThrow(
                () -> new CustomException(ROLE_NOT_FOUND));

        // 유효성 검사
        validateUserId(stylistRequestDto.getUserId());
        validateEmail(stylistRequestDto.getEmail());
        validatePassword(stylistRequestDto.getPassword());
        validateNickname(stylistRequestDto.getNickname());

        Stylist stylist = stylistRequestDto.toStylist(passwordEncoder);
        stylist.setRoles(Collections.singleton(userRole));

        return StylistResponseDto.toDto(stylistRepository.save(stylist));
    }

    @Transactional
    public TokenResponseDto login(LoginDto loginDto) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenResponseDto tokenResDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenResDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 토큰 발급
        return tokenResDto;
    }

    public void validateUserId(String userId) {
        if (stylistRepository.existsByUserId(userId)) {
            throw new CustomException(ALREADY_SAVED_ID);
        }

        Pattern userIdPattern = Pattern.compile("^[a-z0-9]{6,20}$");
        Matcher userIdMatcher = userIdPattern.matcher(userId);
        if (!userIdMatcher.matches()) {
            throw new CustomException(INVALID_ID);
        }
    }

    public void validateEmail(String email) {
        if (stylistRepository.existsByEmail(email)) {
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
        if (stylistRepository.existsByNickname(nickname)) {
            throw new CustomException(ALREADY_SAVED_NICKNAME);
        }

        Pattern nicknamePattern = Pattern.compile("^[가-힣a-z0-9]{2,10}$");
        Matcher nicknameMatcher = nicknamePattern.matcher(nickname);
        if (!nicknameMatcher.matches()) {
            throw new CustomException(INVALID_NICKNAME);
        }
    }


    public String changePassword(Stylist stylist,String password, String code) throws Exception{
        VerificationToken verificationToken = verificationTokenRepository.findByVerificationCode(code);
        if (verificationToken == null) return "인증번호가 일치하지 않습니다.";
        if(!verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            return "인증 시간이 초과 되었습니다.";
        }
        if(stylist == null) throw new Exception("changePassword(),stylist가 조회되지 않음");
        stylist.setPassword(passwordEncoder.encode(password));
        verificationTokenRepository.delete(verificationToken);
        stylistRepository.save(stylist);
        return "비밀 번호가 변경 되었습니다.";
    }

}
