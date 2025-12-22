package com.example.banktransfer.account.repository;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByHolderName(String holderName);
    Optional<Account> findByHolderNameAndStatus(String holderName, AccountStatus status);
    Optional<Account> findByIdAndStatus(Long id, AccountStatus status);
    Optional<Account> findByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);
    Optional<Account> findByUserIdAndBankCodeAndAccountNumberAndStatus(
            Long userId,
            String bankCode,
            String accountNumber,
            AccountStatus status
    );
    boolean existsByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);
}
