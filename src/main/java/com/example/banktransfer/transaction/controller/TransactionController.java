package com.example.banktransfer.transaction.controller;

import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransactionResponse;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
@Tag(name = "거래 생성 API", description = "입금, 출금, 이체 및 거래 기록 조회 서비스")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "계좌 입금 API",
            description = "특정 계좌에 금액을 입금."
    )
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
        @PathVariable Long accountId,
        @Valid @RequestBody MoneyRequest request) {

        Transaction tx = transactionService.deposit(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }

    @Operation(
            summary = "계좌 출금 API",
            description = "계좌 당 일 한도 1일 최대 1,000,000원 제한 출금."
    )
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
        @PathVariable Long accountId,
        @Valid @RequestBody MoneyRequest request) {

        Transaction tx = transactionService.withdraw(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }

    @Operation(
            summary = "계좌 이체 API",
            description = "계좌 당 일 한도 1일 최대 3,000,000원 제한 출금 (수수료 1%)"
    )
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long accountId,
            @Valid @RequestBody TransferRequest request) {

        Transaction tx = transactionService.transfer(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(tx));
    }

    @Operation(
            summary = "거래내역 조회 API",
            description = "송금 및 수취 내역 최신순 조회"
    )
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }
}
