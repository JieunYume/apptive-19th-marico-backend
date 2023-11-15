package com.apptive.marico.service.auth;

import com.apptive.marico.dto.LoginDto;
import com.apptive.marico.dto.token.TokenRequestDto;
import com.apptive.marico.dto.token.TokenResponseDto;
import com.apptive.marico.entity.token.RefreshToken;
import com.apptive.marico.jwt.TokenProvider;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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

    @Transactional
    public void logout(TokenRequestDto tokenReqDto) {
        // 로그아웃하려는 사용자의 정보를 가져옴
        Authentication authentication = tokenProvider.getAuthentication(tokenReqDto.getAccessToken());

        // 저장소에서 해당 사용자의 refresh token 삭제
        refreshTokenRepository.deleteByKey(authentication.getName());
    }

}
