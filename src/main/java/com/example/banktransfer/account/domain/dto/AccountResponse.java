package com.example.banktransfer.account.domain.dto;

import com.example.banktransfer.account.domain.entity.Account;

public record AccountResponse(
        Long id,
        Long userId,
        String status,
        String bankCode,
        String accountNumber,
        String holderName
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getUserId(),
                a.getStatus().name(),
                a.getBankCode(),
                a.getAccountNumber(),
                a.getHolderName()
        );
    }
}
