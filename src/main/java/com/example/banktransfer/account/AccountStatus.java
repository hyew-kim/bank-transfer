package com.example.banktransfer.account;

import lombok.Getter;

public enum AccountStatus {
    ACTIVE("활성", true)
    , CLOSED("해지", false);

    private final String description;
    @Getter
    private final Boolean isChangeable;

    AccountStatus(String description, Boolean isChangeable) {
        this.description = description;
        this.isChangeable = isChangeable;
    }
}
