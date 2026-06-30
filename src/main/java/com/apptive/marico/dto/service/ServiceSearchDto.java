package com.apptive.marico.dto.service;

import com.apptive.marico.entity.service.ServiceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSearchDto {

    private ServiceType serviceType;
    private String styleCategory;
    private Integer minPrice;
    private Integer maxPrice;
}
