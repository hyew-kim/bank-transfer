package com.example.banktransfer.transaction;

import lombok.Getter;

import java.math.BigDecimal;

public enum TransactionType {
    DEPOSIT("입금", BigDecimal.ZERO)
    , WITHDRAW("출금", BigDecimal.ZERO)
    , TRANSFER("이체", BigDecimal.valueOf(0.01));

    private String description;
    @Getter
    private BigDecimal feeRate;

    TransactionType(String description, BigDecimal feeRate) {
        this.description = description;
        this.feeRate = feeRate;
    }
}
