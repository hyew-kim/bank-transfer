package com.example.banktransfer.transaction.domain.dto;

import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        // 거래 식별 정보
        String transactionId,           // "TRX20251218210309"
        TransactionType type,            // DEPOSIT, WITHDRAW, TRANSFER
        TransactionStatus status,        // SUCCESS, FAILED, PENDING

        // 거래 금액 정보
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal balanceAfter,         // 거래 후 잔액

        // 계좌 정보
        String fromAccountNumber,        // 출금 계좌
        String toAccountNumber,          // 입금 계좌 (이체인 경우)

        // 부가 정보
        String description,
        LocalDateTime createdAt,         // 거래 일시

        // 실패 정보 (실패 시에만)
        String failureReason
) {
    // 엔티티 -> DTO 변환 팩토리 메서드
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getAccount().getBalance(),
                transaction.getFromAccountNumber(),
                transaction.getToAccountNumber(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getFailureReason()
        );
    }
}

