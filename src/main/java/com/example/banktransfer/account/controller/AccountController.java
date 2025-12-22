package com.example.banktransfer.account.controller;

import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("")
    public ResponseEntity<String> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        accountService.createAccount(request);
        return ResponseEntity.ok("계좌 등록이 완료되었습니다.");
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.searchAccount(accountId));
    }

    @GetMapping(params = {"userId", "bankCode", "accountNumber"})
    public ResponseEntity<List<AccountResponse>> getAccount(
            @RequestParam @NotNull Long userId,
            @RequestParam String bankCode,
            @RequestParam String accountNumber
    ) {
        return ResponseEntity.ok(accountService.searchAccount(userId, bankCode, accountNumber));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok("계좌 해지가 완료되었습니다.");
    }
}
