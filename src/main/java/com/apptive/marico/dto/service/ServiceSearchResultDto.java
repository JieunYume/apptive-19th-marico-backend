package com.apptive.marico.dto.service;

import com.apptive.marico.dto.stylist.service.ServiceCategoryDto;
import com.apptive.marico.entity.service.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSearchResultDto {

    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private int price;
    private List<ServiceCategoryDto> serviceCategories;
    private Long stylistId;
    private String stageName;
    private String stylistProfileImage;

    public static ServiceSearchResultDto from(Service service) {
        List<ServiceCategoryDto> categoryDtos = service.getServiceCategories().stream()
                .map(ServiceCategoryDto::toDto)
                .collect(Collectors.toList());

        return ServiceSearchResultDto.builder()
                .serviceId(service.getId())
                .serviceName(service.getServiceName())
                .serviceDescription(service.getServiceDescription())
                .price(service.getPrice())
                .serviceCategories(categoryDtos)
                .stylistId(service.getStylist().getId())
                .stageName(service.getStylist().getStageName())
                .stylistProfileImage(service.getStylist().getProfileImage())
                .build();
    }
}
