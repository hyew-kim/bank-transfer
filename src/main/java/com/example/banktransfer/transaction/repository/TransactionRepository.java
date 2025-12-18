package com.example.banktransfer.transaction.repository;

import com.example.banktransfer.transaction.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
}
