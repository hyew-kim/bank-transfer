package com.example.banktransfer.transaction.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.account.service.AccountValidatorService;
import com.example.banktransfer.global.exception.BusinessException;
import com.example.banktransfer.transaction.exception.InvalidInputException;
import com.example.banktransfer.global.progress.ProgressRecorder;
import com.example.banktransfer.global.progress.ProgressStatus;
import com.example.banktransfer.global.support.OptimisticLockingRetryExecutor;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransactionResponse;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final AccountValidatorService accountValidatorService;
    private final AccountRepository accountRepository;
    private final OptimisticLockingRetryExecutor optimisticLockingRetryExecutor;
    private final TransactionRecordService transactionRecordService;
    private final TransactionRepository transactionRepository;
    private final ProgressRecorder progressRecorder;

    public Transaction deposit(Long accountId, MoneyRequest request) {
        // 1. 거래 생성 (PENDING)
        Account account = accountValidatorService.getAccountOrThrow(accountId);

        Transaction tx = transactionRecordService.createPending(
                account,
                request.amount(),
                request.description(),
                TransactionType.DEPOSIT,
                null
        );
        progressRecorder.record(tx.getTransactionId(), ProgressStatus.PENDING, null);

        try {
            if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidInputException();
            }
            // 2. 잔고 업데이트
            optimisticLockingRetryExecutor.run(() -> {
                Account accountForUpdate = accountValidatorService.getAccountOrThrow(accountId);
                accountForUpdate.changeBalance(accountForUpdate.getBalance().add(request.amount()));
            });

            // 3. 성공 처리
            transactionRecordService.markSuccess(tx.getTransactionId());
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.SUCCESS, null);
            log.info("입금 완료:: {}", tx.getTransactionId());
        } catch (BusinessException ex) {
            // 4. 실패 처리
            transactionRecordService.markFailed(tx.getTransactionId(), null);
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);

            throw ex;
        } catch (Exception ex) {
            transactionRecordService.markFailed(tx.getTransactionId(), null);
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);

            throw ex;
        }

        return tx;
    }

    public Transaction withdraw(Long accountId, MoneyRequest request) {
        Account account = accountValidatorService.getAccountOrThrow(accountId);

        Transaction tx = transactionRecordService.createPending(
                account,
                request.amount(),
                request.description(),
                TransactionType.WITHDRAW,
                null
        );
        progressRecorder.record(tx.getTransactionId(), ProgressStatus.PENDING, null);

        try {
            optimisticLockingRetryExecutor.run(() -> {
                Account accountForUpdate = accountValidatorService.getAccountOrThrow(accountId);
                accountValidatorService.validateWithdrawal(accountForUpdate, request.amount());
                // 2. 잔고 업데이트
                accountForUpdate.changeBalance(accountForUpdate.getBalance().subtract(request.amount()));
            });
            // 3. 성공 처리
            transactionRecordService.markSuccess(tx.getTransactionId());
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.SUCCESS, null);
            log.info("출금 완료:: {}", tx.getTransactionId());

        } catch (BusinessException ex) {
            // 4. 실패 처리
            transactionRecordService.markFailed(tx.getTransactionId(), null);
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);

            throw ex;
        } catch (Exception ex) {
            transactionRecordService.markFailed(tx.getTransactionId(), null);
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);

            throw ex;
        }

        return tx;
    }

    public Transaction transfer(Long accountId, TransferRequest request) {
        Account account = accountValidatorService.getAccountOrThrow(accountId);
        Transaction tx = null;

        try {
            Account toAccount = accountValidatorService.getAccountOrThrow(request.toAccountId());

            tx = transactionRecordService.createPending(
                    account,
                    request.amount(),
                    request.description(),
                    TransactionType.TRANSFER,
                    toAccount
            );
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.PENDING, null);

            BigDecimal fee = tx.getFee();
            optimisticLockingRetryExecutor.run(() -> {
                Account fromAccount = accountValidatorService.getAccountOrThrow(accountId);
                Account targetAccount = accountValidatorService.getAccountOrThrow(request.toAccountId());
                accountValidatorService.validateTransfer(fromAccount, request.amount().add(fee));
                // 2. 잔고 업데이트
                fromAccount.changeBalance(fromAccount.getBalance().subtract(request.amount()).subtract(fee));
                targetAccount.changeBalance(targetAccount.getBalance().add(request.amount()));
                accountRepository.save(fromAccount);
                accountRepository.save(targetAccount);
            });

            log.info("출금 완료:: {}", tx.getTransactionId());
            log.info("입금 완료:: {}", tx.getTransactionId());

            // 3. 성공 처리
            transactionRecordService.markSuccess(tx.getTransactionId());
            progressRecorder.record(tx.getTransactionId(), ProgressStatus.SUCCESS, null);
            log.info("이체 완료:: {}", tx.getTransactionId());
        } catch (BusinessException ex) {
            if (tx == null) {
                tx = transactionRecordService.createFailed(
                        account,
                        request.amount(),
                        request.description(),
                        TransactionType.TRANSFER,
                        null,
                        null
                );
                progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);
            } else {
                // 4. 실패 처리
                transactionRecordService.markFailed(tx.getTransactionId(), null);
                progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);
            }

            throw ex;
        } catch (Exception ex) {
            if (tx == null) {
                tx = transactionRecordService.createFailed(
                        account,
                        request.amount(),
                        request.description(),
                        TransactionType.TRANSFER,
                        null,
                        null
                );
                progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);
            } else {
                transactionRecordService.markFailed(tx.getTransactionId(), null);
                progressRecorder.record(tx.getTransactionId(), ProgressStatus.FAILED, null);
            }

            throw ex;
        }

        return tx;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndStatusOrderByIdDesc(accountId, TransactionStatus.SUCCESS)
                .orElse(List.of());

        return transactions.stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
