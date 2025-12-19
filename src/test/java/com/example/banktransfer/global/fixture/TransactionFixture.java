package com.example.banktransfer.global.fixture;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.entity.Transaction;

import java.math.BigDecimal;

public class TransactionFixture {
    public static Transaction createDepositTransaction(Account account, BigDecimal amount, String description) {
        return Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .toAccountNumber(account.getAccountNumber())
                .toHolderName(account.getHolderName())
                .fromAccountNumber(null)
                .fromHolderName(null)
                .description(description)
                .fee(amount.multiply(TransactionType.DEPOSIT.getFeeRate()))
                .feeRate(TransactionType.DEPOSIT.getFeeRate())
                .account(account)
                .build();
    }
}
