package com.apptive.marico.service;

import com.apptive.marico.dto.service.ServiceSearchDto;
import com.apptive.marico.dto.service.ServiceSearchResultDto;
import com.apptive.marico.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceService {

    private final ServiceRepository stylistServiceRepository;

    public List<ServiceSearchResultDto> search(ServiceSearchDto dto) {
        return stylistServiceRepository.searchByFilters(
                        dto.getServiceType(),
                        dto.getStyleCategory(),
                        dto.getMinPrice(),
                        dto.getMaxPrice()
                ).stream()
                .map(ServiceSearchResultDto::from)
                .collect(Collectors.toList());
    }
}
