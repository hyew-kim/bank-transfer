package com.example.banktransfer.account.domain.dto;

import com.example.banktransfer.account.domain.entity.Account;

public record AccountResponse(
        Long id,
        String status,
        String accountNumber,
        String holderName
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getStatus().name(),
                a.getAccountNumber(),
                a.getHolderName()
        );
    }
}
