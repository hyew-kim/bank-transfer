package com.example.banktransfer.transaction;

public enum TransactionStatus {
    PENDING("처리중"),
    SUCCESS("성공"),
    FAILED("실패"),
    CANCELLED("취소"),
    TIMEOUT("시간초과");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }
}