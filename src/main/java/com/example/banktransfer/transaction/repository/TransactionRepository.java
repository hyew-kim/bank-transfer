package com.example.banktransfer.transaction.repository;

import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<List<Transaction>> findByAccountIdAndStatusOrderByIdDesc(Long accountId, TransactionStatus status);
}
