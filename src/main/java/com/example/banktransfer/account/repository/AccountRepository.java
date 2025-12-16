package com.example.banktransfer.account.repository;

import com.example.banktransfer.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByHolderName(String holderName);
}
