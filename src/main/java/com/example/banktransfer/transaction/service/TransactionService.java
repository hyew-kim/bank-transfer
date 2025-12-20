package com.example.banktransfer.transaction.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.account.service.BalanceValidatorService;
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
    private final BalanceValidatorService balanceValidatorService;
    private final AccountRepository accountRepository;
    private final TransactionRecordService transactionRecordService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction deposit(Long accountId, MoneyRequest request) {
        // 1. 거래 생성 (PENDING)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("유저의 계좌 정보를 찾을 수 없습니다."));

        Transaction tx = transactionRecordService.createPending(
                account,
                request.amount(),
                request.description(),
                TransactionType.DEPOSIT,
                null
        );

        try {
            if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
            }
            // 2. 잔고 업데이트
            account.changeBalance(account.getBalance().add(request.amount()));

            // 3. 성공 처리
            transactionRecordService.markSuccess(tx.getTransactionId());
            log.info("입금 완료:: {}", tx.getTransactionId());
        } catch (Exception ex) {
            // 4. 실패 처리
            transactionRecordService.markFailed(tx.getTransactionId(), ex.getMessage());

            throw new RuntimeException(tx.getTransactionId(), ex);
        }

        return tx;
    }

    @Transactional
    public Transaction withdraw(Long accountId, MoneyRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("유저의 계좌 정보를 찾을 수 없습니다."));

        Transaction tx = transactionRecordService.createPending(
                account,
                request.amount(),
                request.description(),
                TransactionType.WITHDRAW,
                null
        );

        try {
            if (balanceValidatorService.validateWithdrawal(accountId, request.amount())) {
                // 2. 잔고 업데이트
                account.changeBalance(account.getBalance().subtract(request.amount()));
                // 3. 성공 처리
                transactionRecordService.markSuccess(tx.getTransactionId());
                log.info("출금 완료:: {}", tx.getTransactionId());
            }

        } catch (Exception ex) {
            // 4. 실패 처리
            transactionRecordService.markFailed(tx.getTransactionId(), ex.getMessage());

            throw new RuntimeException(tx.getTransactionId(), ex);
        }

        return tx;
    }

    @Transactional
    public Transaction transfer(Long accountId, TransferRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("유저의 계좌 정보를 찾을 수 없습니다."));

        Transaction tx = null;

        try {
            Account toAccount = accountRepository.findById(request.toAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("받는 계좌가 유효하지 않습니다."));

            tx = transactionRecordService.createPending(
                    account,
                    request.amount(),
                    request.description(),
                    TransactionType.TRANSFER,
                    toAccount
            );

            BigDecimal fee = tx.getFee();
            if (balanceValidatorService.validateTransfer(accountId, request.amount().add(fee))) {
                // 2. 잔고 업데이트
                account.changeBalance(account.getBalance().subtract(request.amount()).subtract(fee));
                accountRepository.save(account);

                log.info("출금 완료:: {}", tx.getTransactionId());

                toAccount.changeBalance(toAccount.getBalance().add(request.amount()));
                accountRepository.save(toAccount);
                log.info("입금 완료:: {}", tx.getTransactionId());

                // 3. 성공 처리
                transactionRecordService.markSuccess(tx.getTransactionId());
                log.info("이체 완료:: {}", tx.getTransactionId());
            }
        } catch (Exception ex) {
            if (tx == null) {
                tx = transactionRecordService.createPending(
                        account,
                        request.amount(),
                        request.description(),
                        TransactionType.TRANSFER,
                        null
                );
            }
            // 4. 실패 처리
            transactionRecordService.markFailed(tx.getTransactionId(), ex.getMessage());

            throw new RuntimeException(tx.getTransactionId(), ex);
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
