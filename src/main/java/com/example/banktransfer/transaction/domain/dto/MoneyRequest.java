package com.example.banktransfer.transaction.domain.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MoneyRequest(
        @NotNull @Positive BigDecimal amount,
        String description
) {}