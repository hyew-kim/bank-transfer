package com.example.banktransfer.transaction.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long toAccountId,
        @NotNull @Positive BigDecimal amount,
        String description
) {}