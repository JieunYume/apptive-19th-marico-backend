package com.apptive.marico.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StylistMatchingDto {

    private AccountDto refundAccount;
    private AccountDto stylistAccount;
    private int price;

}
