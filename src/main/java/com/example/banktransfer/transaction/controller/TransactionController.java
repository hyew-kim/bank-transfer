package com.example.banktransfer.transaction.controller;

import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransactionResponse;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
        @PathVariable Long accountId,
        @Valid @RequestBody MoneyRequest request) {

        Transaction tx = transactionService.deposit(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
        @PathVariable Long accountId,
        @Valid @RequestBody MoneyRequest request) {

        Transaction tx = transactionService.withdraw(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
        @PathVariable Long accountId,
        @Valid @RequestBody TransferRequest request) {

        Transaction tx = transactionService.transfer(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }
  /*  @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long accountId) {

    }*/
}
