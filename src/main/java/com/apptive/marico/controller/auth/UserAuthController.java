package com.apptive.marico.controller.auth;


import com.apptive.marico.dto.LoginDto;
import com.apptive.marico.dto.finId.UserFindIdResponseDto;
import com.apptive.marico.dto.findPwd.ChangePwdResponseDto;
import com.apptive.marico.dto.findPwd.NewPwdRequestDto;
import com.apptive.marico.dto.finId.SendEmailRequestDto;
import com.apptive.marico.dto.token.TokenRequestDto;
import com.apptive.marico.dto.token.TokenResponseDto;
import com.apptive.marico.dto.verificationToken.VerificationTokenRequestDto;
import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.repository.MemberRepository;
import com.apptive.marico.repository.StylistRepository;
import com.apptive.marico.service.auth.MemberAuthService;
import com.apptive.marico.service.auth.StylistAuthService;
import com.apptive.marico.service.auth.UserAuthService;
import com.apptive.marico.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserAuthController {
    private final VerificationTokenService verificationTokenService;
    private final StylistRepository stylistRepository;
    private final MemberRepository memberRepository;
    private final StylistAuthService stylistAuthService;
    private final MemberAuthService memberAuthService;
    private final UserAuthService userAuthService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(userAuthService.login(loginDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRequestDto requestDto) {
        userAuthService.logout(requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody SendEmailRequestDto sendEmailRequestDto) {
        String email = sendEmailRequestDto.getEmail();
        try {
            verificationTokenService.createVerificationToken(email);
            return ResponseEntity.ok("인증 번호가 전송 되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/findId/check-code")
    public ResponseEntity<UserFindIdResponseDto> checkCodeForId(@RequestBody VerificationTokenRequestDto verificationTokenRequestDto) {
        return ResponseEntity.ok(verificationTokenService.verifyUserEmailForId(verificationTokenRequestDto.getCode()));
    }
    @PostMapping("/findPwd/check-code")
    public ResponseEntity<Boolean> checkCodeForPwd(@RequestBody VerificationTokenRequestDto verificationTokenRequestDto) {
        return ResponseEntity.ok(verificationTokenService.verifyUserEmailForPwd(verificationTokenRequestDto.getCode()));
    }

    @PostMapping("/changePwd")
    public ResponseEntity<ChangePwdResponseDto> checkCodeForPwd(@RequestBody NewPwdRequestDto newPwdRequestDto) throws Exception {
        Optional<Stylist> findStylist = stylistRepository.findByUserId(newPwdRequestDto.getUserId());
        Optional<Member> findMember = memberRepository.findByUserId(newPwdRequestDto.getUserId());
        if(findStylist.isPresent()) {
            return ResponseEntity.ok(new ChangePwdResponseDto(stylistAuthService.changePassword(findStylist.get(), newPwdRequestDto.getPassword(), newPwdRequestDto.getCode())));
        }

        else if (findMember.isPresent()) {
            return ResponseEntity.ok(new ChangePwdResponseDto(memberAuthService.changePassword(findMember.get(), newPwdRequestDto.getPassword(), newPwdRequestDto.getCode())));
        }
        else {
            return ResponseEntity.ok(new ChangePwdResponseDto("user를 찾을 수 없습니다."));
        }
    }
}
