package com.apptive.marico.controller;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.service.ServiceScheduleService;
import com.apptive.marico.utils.ApiUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ServiceScheduleController {

    private final ServiceScheduleService serviceScheduleService;

    @PostMapping("/matching/{matchingId}/content/{serviceContentId}")
    public ResponseEntity<?> bookSchedule(
            Principal principal,
            @PathVariable Long matchingId,
            @PathVariable Long serviceContentId,
            @Valid @RequestBody ServiceScheduleRequestDto dto
    ) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(
                serviceScheduleService.bookSchedule(principal.getName(), matchingId, serviceContentId, dto)
        ));
    }
}
