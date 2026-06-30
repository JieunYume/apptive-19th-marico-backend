package com.apptive.marico.dto.stylist.service;

import com.apptive.marico.entity.service.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StylistServiceDto {
    private Long service_id;
    private String serviceName;
    private String serviceDescription;
    private List<ServiceCategoryDto> serviceCategories;
    private int price;

    public static StylistServiceDto toDto(Service stylistService) {
        List<ServiceCategoryDto> categoryDtos = stylistService.getServiceCategories().stream()
                .map(ServiceCategoryDto::toDto)
                .collect(Collectors.toList());

        return StylistServiceDto.builder()
                .service_id(stylistService.getId())
                .serviceName(stylistService.getServiceName())
                .serviceDescription(stylistService.getServiceDescription())
                .serviceCategories(categoryDtos)
                .price(stylistService.getPrice())
                .build();
    }
}
