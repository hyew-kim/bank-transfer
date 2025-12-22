package com.example.banktransfer.account.service;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.progress.ProgressRecorder;
import com.example.banktransfer.global.progress.ProgressStatus;
import com.example.banktransfer.global.support.OptimisticLockingRetryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final OptimisticLockingRetryExecutor optimisticLockingRetryExecutor;
    private final ProgressRecorder progressRecorder;

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

    public void createAccount(CreateAccountRequest request) {
        String progressKey = "account:create:" + request.holderName();
        ProgressStatus currentStatus = progressRecorder.getStatus(progressKey);
        if (ProgressStatus.PROCESSING == currentStatus) {
            throw new RuntimeException("이미 개설 진행 중입니다.");
        }
        progressRecorder.record(progressKey, ProgressStatus.PROCESSING, null);

        try {
            Account account = optimisticLockingRetryExecutor.execute(() -> {
                Account newAccount = Account.builder()
                        .holderName(request.holderName())
                        .build();

                return accountRepository.save(newAccount);
            });
            progressRecorder.record(progressKey, ProgressStatus.SUCCESS, "accountId=" + account.getId());
            progressRecorder.delete(progressKey);
        } catch (Exception ex) {
            progressRecorder.record(progressKey, ProgressStatus.FAILED, ex.getMessage());
            throw ex;
        }
    }

    public void closeAccount(Long accountId) {
        String progressKey = "account:close:" + accountId;
        ProgressStatus currentStatus = progressRecorder.getStatus(progressKey);
        if (ProgressStatus.PROCESSING == currentStatus) {
            throw new RuntimeException("이미 해지 진행 중입니다.");
        }
        progressRecorder.record(progressKey, ProgressStatus.PROCESSING, null);

        try {
            optimisticLockingRetryExecutor.run(() -> {
                Account userAccount = accountRepository
                        .findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account does not exists:: accountId - " + accountId));

                userAccount.changeAccountStatus(AccountStatus.CLOSED);
            });
            progressRecorder.record(progressKey, ProgressStatus.SUCCESS, null);
            progressRecorder.delete(progressKey);
        } catch (Exception ex) {
            progressRecorder.record(progressKey, ProgressStatus.FAILED, ex.getMessage());
            throw ex;
        }
    }
}
