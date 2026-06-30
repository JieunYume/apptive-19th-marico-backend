package com.apptive.marico.controller;

import com.apptive.marico.dto.stylistService.StylistFilterDto;
import com.apptive.marico.service.MemberHomeService;
import com.apptive.marico.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/home/member")
@RequiredArgsConstructor
public class MemberHomeController {
    private final MemberHomeService memberHomeService;

    @GetMapping("/recommend")
    public ResponseEntity<?> recommendStylist(Principal principal) {
        return ResponseEntity.ok(memberHomeService.recommendStylist(principal.getName()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchStylist(
            @RequestParam(required = false, defaultValue = "") String style,
            @RequestParam(required = false, defaultValue = "") String city,
            @RequestParam(required = false, defaultValue = "") String state,
            @RequestParam(required = false, defaultValue = "") String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        StylistFilterDto filter = StylistFilterDto.builder()
                .style(style)
                .city(city)
                .state(state)
                .gender(gender)
                .build();
        return ResponseEntity.ok(memberHomeService.searchStylist(filter, PageRequest.of(page, size)));
    }

    @PostMapping("/service/{service_id}")
    public ResponseEntity<?> applyService(Principal principal, @PathVariable("service_id") Long serviceId) {
        return ResponseEntity.ok(ApiUtils.success(memberHomeService.applyService(principal.getName(), serviceId)));
    }
}
