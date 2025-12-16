package com.example.banktransfer.account.domain.dto;

public record CreateAccountRequest(
        String holderName
) {
    public static CreateAccountRequest of (String holderName) {
        return new CreateAccountRequest(holderName);
    }
}