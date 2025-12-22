package com.example.banktransfer.account.service;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.exception.AccountClosedException;
import com.example.banktransfer.account.exception.AccountOwnershipException;
import com.example.banktransfer.account.exception.InvalidAccountException;
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
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(InvalidAccountException::new);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException(accountId);
        }

        return account;
    }

    public void validateWithdrawal(Account account, BigDecimal amount) {
        BigDecimal remainingLimit = account.getDailyLimitOfWithdrawal();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new DailyLimitExceededException();
        }

        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountOwnershipException.InsufficientBalanceException();
        }
    }

    public void validateTransfer(Account account, BigDecimal amount) {
        BigDecimal remainingLimit = account.getDailyLimitOfTransfer();

        if (amount.compareTo(remainingLimit) > 0) {
            throw new DailyLimitExceededException();
        }

        BigDecimal balance = account.getBalance();
        BigDecimal expectedBalance = balance.subtract(amount);

        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountOwnershipException.InsufficientBalanceException();
        }
    }
}
