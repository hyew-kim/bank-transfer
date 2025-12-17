package com.example.banktransfer.transaction.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.annotation.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class BalanceValidatorServiceTest {
    @Autowired
    private BalanceValidatorService balanceValidatorService;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        Account account1 = new Account("Junit-tester");
        account1.changeBalance(new BigDecimal("100000"));
        Account account2 = new Account("Junit-tester-2");
        account2.changeBalance(new BigDecimal("9999999999"));

        accountRepository.save(account1);
        accountRepository.save(account2);
    }

    @Test
    public void 잔액이_충분한_계좌_출금검증_성공() {
        assertThat(balanceValidatorService.validateWithdrawal(1L, BigDecimal.valueOf(3000)))
                .isEqualTo(true);
    }

    @Test
    public void 잔액부족_출금() {
        assertThatThrownBy(() -> balanceValidatorService
                .validateWithdrawal(1L, BigDecimal.valueOf(888888)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔액 부족");
    }

    @Test
    public void 잔액부족_송금() {
        assertThatThrownBy(() -> balanceValidatorService
                .validateTransfer(1L, BigDecimal.valueOf(1000000)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔액 부족");
    }

    @Test
    public void 한도초과_출금() {
        assertThatThrownBy(() -> balanceValidatorService
                .validateWithdrawal(1L, BigDecimal.valueOf(10000001)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일 한도");
    }

    @Test
    public void 한도초과_송금() {
        assertThatThrownBy(() -> balanceValidatorService
                .validateTransfer(1L, BigDecimal.valueOf(30000001)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일 한도");
    }
}