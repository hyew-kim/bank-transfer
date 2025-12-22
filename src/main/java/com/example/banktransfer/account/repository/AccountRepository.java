package com.example.banktransfer.account.repository;

import com.example.banktransfer.account.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByHolderName(String holderName);
    Optional<Account> findByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);
    boolean existsByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);
}
