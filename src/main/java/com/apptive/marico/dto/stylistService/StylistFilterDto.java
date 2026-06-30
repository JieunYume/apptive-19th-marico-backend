package com.apptive.marico.dto.stylistService;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StylistFilterDto {

    private String style;
    private String city;
    private String state;
    private String gender;
}
