package com.example.banktransfer.account.controller;

import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("")
    public ResponseEntity<String> createAccount(@RequestBody CreateAccountRequest request) {
        try {
            accountService.createAccount(request);
            return ResponseEntity.ok("계좌 개설이 완료되었습니다.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.searchAccount(accountId));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        try {
            accountService.closeAccount(accountId);
            return ResponseEntity.ok("계좌 해지가 완료되었습니다.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
