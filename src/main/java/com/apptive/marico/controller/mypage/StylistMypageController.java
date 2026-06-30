package com.apptive.marico.controller.mypage;

import com.apptive.marico.dto.AccountDto;
import com.apptive.marico.dto.findId.SendEmailRequestDto;
import com.apptive.marico.dto.mypage.member.PasswordDto;
import com.apptive.marico.dto.stylist.DeleteStyleDto;
import com.apptive.marico.dto.stylist.StyleDto;
import com.apptive.marico.dto.stylist.StylistMypageDto;
import com.apptive.marico.dto.stylist.StylistMypageEditDto;
import com.apptive.marico.dto.stylist.service.StylistServiceDto;
import com.apptive.marico.dto.verificationToken.SendEmailResponseDto;
import com.apptive.marico.service.StylistMypageService;
import com.apptive.marico.service.VerificationTokenService;
import com.apptive.marico.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/mypage/stylist")
@RequiredArgsConstructor
public class StylistMypageController {
    private final StylistMypageService stylistMypageService;
    private final VerificationTokenService verificationTokenService;

    // 마이페이지 조회
    @GetMapping
    public ResponseEntity<StylistMypageDto> mypage(Principal principal) {
        return ResponseEntity.ok(stylistMypageService.mypage(principal.getName()));
    }

    @GetMapping("/information")
    public ResponseEntity<StylistMypageEditDto> loadInf(Principal principal) {
        return ResponseEntity.ok(stylistMypageService.getInformation(principal.getName()));
    }

    @PostMapping("/information")
    public ResponseEntity<?> editInf(Principal principal, @RequestPart MultipartFile profileImage, @RequestPart StylistMypageEditDto stylistMypageEditDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.editInformation(principal.getName(), profileImage ,stylistMypageEditDto)));
    }

    @GetMapping("/stylist-service")
    public ResponseEntity<?> loadServiceList(Principal principal) {
        return ResponseEntity.ok(stylistMypageService.getServiceList(principal.getName()));
    }

    @PostMapping("/stylist-service")
    public ResponseEntity<?> addService(Principal principal, @RequestBody StylistServiceDto stylistServiceDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.addService(principal.getName(), stylistServiceDto)));
    }

    @GetMapping("/stylist-service/{service_id}")
    public ResponseEntity<?> loadService(Principal principal, @PathVariable("service_id") Long service_id) {
        return ResponseEntity.ok(stylistMypageService.getService(principal.getName(), service_id));
    }

    @PatchMapping("/stylist-service/{service_id}")
    public ResponseEntity<?> editService(Principal principal, @PathVariable("service_id") Long service_id, @RequestBody StylistServiceDto stylistServiceDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.editService(principal.getName(), service_id, stylistServiceDto)));
    }

    @DeleteMapping("/stylist-service/{service_id}")
    public ResponseEntity<?> deleteService(Principal principal, @PathVariable("service_id") Long service_id) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.deleteService(principal.getName(), service_id)));
    }

    @GetMapping("/style")
    public ResponseEntity<?> getMyStyle(Principal principal) {
        return ResponseEntity.ok(stylistMypageService.getStyle(principal.getName()));
    }

    @PostMapping("/style")
    public ResponseEntity<?> addMyStyle(Principal principal,@RequestPart MultipartFile image ,@RequestPart StyleDto styleDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.addStyle(principal.getName(), image,styleDto)));
    }

    @DeleteMapping("/style")
    public ResponseEntity<?> deleteMyStyle(Principal principal, @RequestBody DeleteStyleDto deleteStyleDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.deleteStyle(principal.getName(), deleteStyleDto)));
    }

    @GetMapping("/password")
    public ResponseEntity<?> checkPassword(Principal principal, @RequestParam String currentPassword) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.CheckCurrentPassword(principal.getName(), currentPassword)));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestParam String newPassword) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.changePassword(principal.getName(), newPassword)));
    }

    @PostMapping("/email/verification-code")
    public ResponseEntity<SendEmailResponseDto> createVerificationCode(@RequestBody SendEmailRequestDto sendEmailRequestDto) {
        return ResponseEntity.ok(new SendEmailResponseDto(verificationTokenService.createVerificationTokenForSign(sendEmailRequestDto.getEmail())));
    }

    @GetMapping("/email/verification-code")
    public ResponseEntity<?> checkVerificationCode(Principal principal, @RequestParam String code) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(verificationTokenService.verifyUserEmailForSign(code)));
    }

    @PostMapping("/email")
    public ResponseEntity<?> changeEmail(Principal principal, @RequestBody SendEmailRequestDto sendEmailRequestDto) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(stylistMypageService.changeEmail(principal.getName(), sendEmailRequestDto.getEmail())));
    }

    @GetMapping("/delete")
    public ResponseEntity<?> checkPasswordForDelete(Principal principal, @RequestParam String password) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(stylistMypageService.CheckCurrentPassword(principal.getName(), password)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStylist(Principal principal) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(stylistMypageService.deleteStylist(principal.getName())));
    }

    @PatchMapping("/matching/{matchingId}/approve")
    public ResponseEntity<?> approveMatching(Principal principal, @PathVariable Long matchingId) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.approveMatching(principal.getName(), matchingId)));
    }

    @GetMapping("/account")
    public ResponseEntity<?> loadAccount(Principal principal) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.loadAccount(principal.getName())));
    }

    @PostMapping("/account")
    public ResponseEntity<?> addAccount(Principal principal, @RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(ApiUtils.success(stylistMypageService.addAccount(principal.getName(), accountDto)));
    }
}