package com.example.banktransfer.account.service;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Account searchAccount(String holderName) {
        return accountRepository.findByHolderName(holderName);
    }

    @Transactional
    public void createAccount(String holderName) {
        if (searchAccount(holderName) != null) {
            throw new RuntimeException("Account already exists:: 가입자명 - " + holderName);
        }

        Account account = new Account(holderName);

        accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(String accountId) {
        // TODO: 계좌 삭제할때.. 계좌번호 + 계좌 소유주명을 받는게 나을지 검토
        Account userAccount = searchAccount(accountId);

        if (userAccount == null) {
            throw new IllegalArgumentException("Account does not exists:: accountId - " + accountId);
        }

        userAccount.changeAccountStatus(AccountStatus.CLOSED);
    }
}
