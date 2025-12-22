package com.example.banktransfer.global.fixture;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.entity.Account;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public class AccountFixture {

    // 잔액 많은 계좌 (일반적인 테스트용)
    public static Account createRichAccount(String name) {
        return Account.builder()
                .userId(1L)
                .bankCode("777")
                .accountNumber(randomAccountNumber())
                .holderName(name)
                .balance(BigDecimal.valueOf(10000000))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // 잔액 부족한 계좌 (잔액 부족 테스트용)
    public static Account createPoorAccount(String name) {
        return Account.builder()
                .userId(1L)
                .bankCode("777")
                .accountNumber(randomAccountNumber())
                .holderName(name)
                .balance(BigDecimal.valueOf(0))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // 비활성 계좌 (상태 검증 테스트용)
    public static Account createInactiveAccount(String name) {
        return Account.builder()
                .userId(1L)
                .bankCode("777")
                .accountNumber(randomAccountNumber())
                .holderName(name)
                .balance(BigDecimal.valueOf(1000000))
                .status(AccountStatus.CLOSED)
                .build();
    }

    // 커스텀 잔액 계좌
    public static Account createAccountWithBalance(String name, BigDecimal balance) {
        return Account.builder()
                .userId(1L)
                .bankCode("777")
                .accountNumber(randomAccountNumber())
                .holderName(name)
                .balance(balance)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private static String randomAccountNumber() {
        return String.format("%014d", ThreadLocalRandom.current().nextLong(1_0000_0000_0000_00L));
    }
}
