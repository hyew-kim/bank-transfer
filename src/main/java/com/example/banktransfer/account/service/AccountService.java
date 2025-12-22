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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional(readOnly = true)
    public List<AccountResponse> searchAccount(Long userId, String bankCode, String accountNumber) {
        return accountRepository
                .findByUserIdAndBankCodeAndAccountNumber(userId, bankCode, accountNumber)
                .map(AccountResponse::from)
                .stream()
                .toList();
    }

    public void createAccount(CreateAccountRequest request) {
        String progressKey = String.format(
                "account:%d:%s:%s",
                request.userId(),
                request.bankCode(),
                request.accountNumber()
        );
        boolean started = progressRecorder.tryStart(progressKey);
        if (!started) {
            throw new IllegalStateException("이미 등록 진행 중입니다.");
        }

        try {
            if (accountRepository.existsByUserIdAndBankCodeAndAccountNumber(
                    request.userId(),
                    request.bankCode(),
                    request.accountNumber()
            )) {
                throw new IllegalStateException("이미 등록된 계좌입니다.");
            }
            Account account = optimisticLockingRetryExecutor.execute(() -> {
                Account newAccount = Account.builder()
                        .userId(request.userId())
                        .bankCode(request.bankCode())
                        .accountNumber(request.accountNumber())
                        .holderName(request.holderName())
                        .build();

                return accountRepository.save(newAccount);
            });
            progressRecorder.record(progressKey, ProgressStatus.SUCCESS, "accountId=" + account.getId());
            progressRecorder.delete(progressKey);
        } catch (DataIntegrityViolationException ex) {
            progressRecorder.record(progressKey, ProgressStatus.FAILED, "이미 등록된 계좌입니다.");
            progressRecorder.delete(progressKey);
            throw new IllegalStateException("이미 등록된 계좌입니다.", ex);
        } catch (Exception ex) {
            progressRecorder.record(progressKey, ProgressStatus.FAILED, ex.getMessage());
            progressRecorder.delete(progressKey);
            throw ex;
        }
    }

    public void closeAccount(Long accountId) {
        String progressKey = "account:close:" + accountId;

        boolean started = progressRecorder.tryStart(progressKey);
        if (!started) {
            throw new IllegalStateException("이미 해지 진행 중입니다.");
        }

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
            progressRecorder.delete(progressKey);
            throw ex;
        }
    }
}
