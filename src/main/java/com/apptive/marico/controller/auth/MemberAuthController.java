package com.apptive.marico.controller.auth;

import com.apptive.marico.dto.member.MemberRequestDto;
import com.apptive.marico.dto.member.MemberResponseDto;
import com.apptive.marico.service.auth.MemberAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/member")
@RequiredArgsConstructor
public class MemberAuthController {
    private final MemberAuthService memberAuthService;
    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDto> signup(@RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberAuthService.signup(memberRequestDto));
    }
}
