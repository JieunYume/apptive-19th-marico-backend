package com.apptive.marico.controller.mypage;

import com.apptive.marico.dto.AccountDto;
import com.apptive.marico.dto.findId.SendEmailRequestDto;
import com.apptive.marico.dto.mypage.member.MemberMypageDto;
import com.apptive.marico.dto.mypage.member.MemberMypageEditDto;
import com.apptive.marico.dto.mypage.member.PasswordDto;
import com.apptive.marico.dto.mypage.member.LikedStylistListDto;
import com.apptive.marico.dto.verificationToken.SendEmailResponseDto;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.service.MemberMypageService;
import com.apptive.marico.service.VerificationTokenService;
import com.apptive.marico.utils.ApiUtils;
import com.apptive.marico.utils.ApiUtils.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

import static com.apptive.marico.exception.ErrorCode.AUTHORIZATION_NOT_FOUND;

@RestController
@RequestMapping("/api/mypage/member")
@RequiredArgsConstructor
public class MemberMypageController {
    private final MemberMypageService memberMyPageService;
    private final VerificationTokenService verificationTokenService;
    // 마이페이지 조회
    @GetMapping("/")
    public ResponseEntity<MemberMypageDto> mypage(Principal principal) {
        return ResponseEntity.ok(memberMyPageService.mypage(principal.getName()));
    }

    // 프로필 사진 수정
    @PatchMapping(value = "/profile-image", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> changeProfileImage(Principal principal, @RequestPart MultipartFile profileImage) {
        return ResponseEntity.ok(memberMyPageService.changeProfileImage(principal.getName(), profileImage));

    }

    // 관심 스타일리스트
    @GetMapping("/liked-stylist")
    public ResponseEntity<LikedStylistListDto> findLikedStylist(Principal principal) {
        return ResponseEntity.ok(memberMyPageService.findLikedStylist(principal.getName()));
    }

    /**
     * 개인 정보 수정
     * @param principal
     * @return
     */
    // 조회
    @GetMapping("/information")
    public ResponseEntity<MemberMypageEditDto> findInformation(Principal principal) {
        return ResponseEntity.ok(memberMyPageService.getInformation(principal.getName()));
    }

    // 수정
    @PatchMapping("/information")
    public ResponseEntity<ApiSuccess<Object>> updateInformation(Principal principal, @RequestBody MemberMypageEditDto memberMypageEditDto) {
        System.out.println(memberMypageEditDto.getBirthDate());

        return ResponseEntity.ok(new ApiSuccess<>(memberMyPageService.updateInformation(principal.getName(), memberMypageEditDto)));
    }

    // 현재 비밀번호가 일치한지 검사
    @GetMapping("/password")
    public ResponseEntity<ApiSuccess<Object>> checkCurrentPassword(Principal principal, @RequestParam String password) {
        return ResponseEntity.ok(new ApiSuccess<>(memberMyPageService.CheckCurrentPassword(principal.getName(), password)));
    }
    // 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody PasswordDto passwordDto) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(memberMyPageService.changePassword(principal.getName(), passwordDto.getPassword())));

    }

    @PostMapping("/email/verification-code")
    public ResponseEntity<SendEmailResponseDto> createVerificationCode(@RequestBody SendEmailRequestDto sendEmailRequestDto) {
        return ResponseEntity.ok(new SendEmailResponseDto(verificationTokenService.createVerificationTokenForChangeEmail(sendEmailRequestDto.getEmail())));
    }

    @GetMapping("/email/verification-code")
    public ResponseEntity<?> checkVerificationCode(Principal principal, @RequestParam String code) {
        System.out.println(code);
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(verificationTokenService.verifyUserEmailForSign(code)));
    }

    @PatchMapping("/email")
    public ResponseEntity<?> changeEmail(Principal principal, @RequestBody SendEmailRequestDto sendEmailRequestDto) {
        if (principal == null) {
            throw new CustomException(AUTHORIZATION_NOT_FOUND);
        }
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(memberMyPageService.changeEmail(principal.getName(), sendEmailRequestDto.getEmail())));
    }

    @GetMapping("/delete")
    public ResponseEntity<?> checkPasswordForDelete(Principal principal, @RequestParam String password) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(memberMyPageService.CheckCurrentPassword(principal.getName(), password)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMember(Principal principal) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(memberMyPageService.deleteMember(principal.getName())));
    }

    @GetMapping("/account")
    public ResponseEntity<?> loadAccount(Principal principal) {
        return ResponseEntity.ok(ApiUtils.success(memberMyPageService.loadAccount(principal.getName())));
    }
    @PostMapping("/account")
    public ResponseEntity<?> addAccount(Principal principal, @RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(ApiUtils.success(memberMyPageService.addAccount(principal.getName(), accountDto)));
    }

}
