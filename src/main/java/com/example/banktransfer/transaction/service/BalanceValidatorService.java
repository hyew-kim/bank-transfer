package com.example.banktransfer.transaction.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceValidatorService {
    private final AccountRepository accountRepository;

    public boolean validateWithdrawal(Long accountId, BigDecimal amount) {
        // 1. 일 한도 검증
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("No account with id: " + accountId));

        BigDecimal remainingLimit = account.getDailyLimitOfWithdrawal();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new RuntimeException("일 한도 1,000,000원 초과::잔여한도: " + remainingLimit);
        }

        // 2. 예상 잔액 검증
        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("잔액 부족::잔액: " + balance);
        }

        return true;
    }

    public boolean validateTransfer(Long accountId, BigDecimal amount) {
        // 1. 일 한도 검증
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("No account with id: " + accountId));

        BigDecimal remainingLimit = account.getDailyLimitOfTransfer();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new RuntimeException("일 한도 3,000,000원 초과::잔여한도: " + remainingLimit);
        }

        // 2. 예상 잔액 검증
        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("잔액 부족::잔액: " +  balance);
        }

        return true;
    }
}
