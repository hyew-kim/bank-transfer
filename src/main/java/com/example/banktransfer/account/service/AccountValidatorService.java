package com.example.banktransfer.account.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.exception.AccountClosedException;
import com.example.banktransfer.account.exception.AccountOwnershipException;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.transaction.exception.DailyLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountValidatorService {
    private final AccountRepository accountRepository;

    public Account getAccountOrThrow(Long accountId) {
        return accountRepository
                .findById(accountId)
                .orElseThrow(AccountClosedException.InvalidAccountException::new);
    }

    public void validateWithdrawal(Account account, BigDecimal amount) {
        // 1. 일 한도 검증
        BigDecimal remainingLimit = account.getDailyLimitOfWithdrawal();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new DailyLimitExceededException();
        }

        // 2. 예상 잔액 검증
        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountOwnershipException.InsufficientBalanceException();
        }
    }

    public void validateTransfer(Account account, BigDecimal amount) {
        // 1. 일 한도 검증
        BigDecimal remainingLimit = account.getDailyLimitOfTransfer();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new DailyLimitExceededException();
        }

        // 2. 예상 잔액 검증
        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountOwnershipException.InsufficientBalanceException();
        }
    }
}
