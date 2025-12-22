package com.example.banktransfer.account.controller;

import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "계좌 관리 API", description = "신규 계좌 등록, 기존 계좌 삭제 및 조회 서비스")
public class AccountController {
    private final AccountService accountService;

    @Operation(
            summary = "신규 계좌 등록 API",
            description = "신규 계좌를 등록합니다. 중복된 계좌는 등록 불가합니다."
    )
    @PostMapping("")
    public ResponseEntity<String> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        accountService.createAccount(request);
        return ResponseEntity.ok("계좌 등록이 완료되었습니다.");
    }

    @Operation(
            summary = "계좌 ID 기준 계좌 단건 조회 API",
            description = "accountID 기준으로 계좌 단건 조회가능합니다."
    )
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.searchAccount(accountId));
    }

    @Operation(
            summary = "계좌 다건 조회 API",
            description = "userID, 은행코드, 계좌번호 기준으로 계좌 다건 조회가능합니다."
    )
    @GetMapping(params = {"userId", "bankCode", "accountNumber"})
    public ResponseEntity<List<AccountResponse>> getAccount(
            @RequestParam @NotNull Long userId,
            @RequestParam String bankCode,
            @RequestParam String accountNumber
    ) {
        return ResponseEntity.ok(accountService.searchAccount(userId, bankCode, accountNumber));
    }

    @Operation(
            summary = "등록된 계좌 삭제 API",
            description = "accountID 기준으로 계좌 삭제 가능합니다(soft delete)."
    )
    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok("계좌 삭제가 완료되었습니다.");
    }
}
