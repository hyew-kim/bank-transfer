package com.example.banktransfer.transaction.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.exception.InvalidAccountException;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.repository.TransactionRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionRecordService {
    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createPending(
            Account account,
            BigDecimal amount,
            String description,
            TransactionType type,
            @Nullable Account toAccount
    ) {
        Transaction tx = Transaction.builder()
                .type(type)
                .amount(amount)
                .toAccountNumber(account.getAccountNumber())
                .toHolderName(account.getHolderName())
                .fromAccountNumber(toAccount != null ? toAccount.getAccountNumber() : null)
                .fromHolderName(toAccount != null ? toAccount.getHolderName() : null)
                .description(description)
                .fee(amount.multiply(type.getFeeRate()))
                .feeRate(type.getFeeRate())
                .account(account)
                .build();

        return transactionRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createFailed(
            Account account,
            BigDecimal amount,
            String description,
            TransactionType type,
            @Nullable Account toAccount,
            String reason
    ) {
        Transaction tx = Transaction.builder()
                .type(type)
                .amount(amount)
                .toAccountNumber(account.getAccountNumber())
                .toHolderName(account.getHolderName())
                .fromAccountNumber(toAccount != null ? toAccount.getAccountNumber() : null)
                .fromHolderName(toAccount != null ? toAccount.getHolderName() : null)
                .description(description)
                .fee(amount.multiply(type.getFeeRate()))
                .feeRate(type.getFeeRate())
                .status(TransactionStatus.FAILED)
                .failureReason(reason)
                .account(account)
                .build();

        return transactionRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String transactionId) {
        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(InvalidAccountException::new);
        tx.setStatus(TransactionStatus.SUCCESS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String transactionId, String reason) {
        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(InvalidAccountException::new);
        tx.setStatus(TransactionStatus.FAILED);
        tx.setFailureReason(reason);
    }
}
