package com.example.banktransfer.account.domain.dto;


import jakarta.validation.constraints.NotEmpty;

public record CreateAccountRequest(
        @NotEmpty Long  userId,
        @NotEmpty String bankCode,
        @NotEmpty String accountNumber,
        @NotEmpty String holderName
) {
    public static CreateAccountRequest of(Long userId, String bankCode, String accountNumber, String holderName) {
        return new CreateAccountRequest(userId, bankCode, accountNumber, holderName);
    }
}
