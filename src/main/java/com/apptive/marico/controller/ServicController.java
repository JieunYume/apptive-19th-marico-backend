package com.apptive.marico.controller;

import com.apptive.marico.dto.service.ServiceSearchDto;
import com.apptive.marico.entity.service.ServiceType;
import com.apptive.marico.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServicController {

    private final ServiceService serviceSearchService;

    @GetMapping("/search")
    public ResponseEntity<?> searchServices(
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) String styleCategory,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        ServiceSearchDto dto = ServiceSearchDto.builder()
                .serviceType(serviceType)
                .styleCategory(styleCategory)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
        return ResponseEntity.ok(serviceSearchService.search(dto));
    }
}
