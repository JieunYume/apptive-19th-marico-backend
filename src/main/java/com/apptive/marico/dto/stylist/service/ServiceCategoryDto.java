package com.apptive.marico.dto.stylist.service;

import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ConnectionType;
import com.apptive.marico.entity.service.ServiceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServiceCategoryDto {

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;

    private String categoryDescription;

    public static ServiceCategoryDto toDto(ServiceContent serviceCategory) {
        return ServiceCategoryDto.builder()
                .serviceType(serviceCategory.getServiceType())
                .connectionType(serviceCategory.getConnectionType())
                .categoryDescription(serviceCategory.getCategoryDescription())
                .build();
    }
}
