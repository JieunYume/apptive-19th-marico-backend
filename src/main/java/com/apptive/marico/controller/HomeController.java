package com.apptive.marico.controller;


import com.apptive.marico.dto.stylistService.StylistFilterDto;
import com.apptive.marico.entity.Stylist;
import com.apptive.marico.service.HomeService;
import com.apptive.marico.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    @GetMapping("/filter")
    public ResponseEntity<?> stylistFilter(@RequestParam(defaultValue = "All") String gender, @RequestParam(defaultValue = "All") String city, @RequestParam(defaultValue = "All") String  style) {
        return ResponseEntity.ok(homeService.filter(StylistFilterDto.builder()
                .gender(gender)
                .city(city)
                .style(style)
                .build()));
    }

    // 스타일리스트 id로 스타일리스트 상세 페이지 조회
    @GetMapping("/stylist/{stylist_id}")
    public ResponseEntity<?> stylistDetailByStylist(@PathVariable Long stylist_id) {
        return ResponseEntity.ok(homeService.stylistDetailByStylist(stylist_id));
    }
    // 스타일 id로 스타일리스트 상세 페이지 조회
    @GetMapping("/stylist/style/{style_id}")
    public ResponseEntity<?> stylistDetailByStyle(@PathVariable Long style_id) {
        return ResponseEntity.ok(homeService.stylistDetailByStyle(style_id));
    }

    // 스타일리스트 서비스 신청 조회
    @GetMapping("/member/matching/{service_id}")
    public ResponseEntity<?> loadService(Principal principal, @PathVariable Long service_id) {
        return ResponseEntity.ok(homeService.loadApplication(principal.getName(), service_id));
    }
    // 스타일리스트 서비스 신청 하기
    @PostMapping("/member/matching/{service_id}")
    public ResponseEntity<?> addRequestService(Principal principal, @PathVariable Long service_id) {
        return ResponseEntity.ok(ApiUtils.success(homeService.addApplication(principal.getName(), service_id)));
    }

}
