package com.example.banktransfer.account.service;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountResponse searchAccount(String holderName) {
        Account account = accountRepository
                .findByHolderName(holderName)
                .orElseThrow(() -> new RuntimeException(holderName + "소유의 계좌를 찾을 수 없습니다."));

        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse searchAccount(Long accountId) {
        Account account =  accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("ID " + accountId + "를 찾을 수 없습니다."));

        return AccountResponse.from(account);
    }

    @Transactional
    public void createAccount(CreateAccountRequest request) {
        String holderName = request.holderName();

        if (accountRepository.existsByHolderName(holderName)) {
            throw new RuntimeException("이미 계좌가 존재합니다.:: 가입자명 - " + holderName);
        }

        Account account = Account.builder()
                .holderName(holderName)
                .build();

        accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(Long accountId) {
        Account userAccount = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account does not exists:: accountId - " + accountId));

        userAccount.changeAccountStatus(AccountStatus.CLOSED);
    }
}
