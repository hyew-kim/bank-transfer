package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.account.service.BalanceValidatorService;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import com.example.banktransfer.global.fixture.AccountFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class BalanceValidatorServiceTest extends BaseIntegrationTest {
    @Autowired
    private BalanceValidatorService balanceValidatorService;
    @Autowired
    private AccountRepository accountRepository;

    final String DEFAULT_TEST_NAME = "balance-tester";
    @Test
    public void 잔액이_충분한_계좌_출금검증_성공() {
        Account testAccount = AccountFixture.createRichAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThat(balanceValidatorService.validateWithdrawal(testAccount.getId(), BigDecimal.valueOf(3000)))
                .isEqualTo(true);
    }

    @Test
    public void 잔액부족_출금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> balanceValidatorService
                .validateWithdrawal(testAccount.getId(), BigDecimal.valueOf(888888)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔액 부족");
    }

    @Test
    public void 잔액부족_송금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> balanceValidatorService
                .validateTransfer(testAccount.getId(), BigDecimal.valueOf(1000000)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔액 부족");
    }

    @Test
    public void 한도초과_출금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> balanceValidatorService
                .validateWithdrawal(testAccount.getId(),BigDecimal.valueOf(10000001)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일 한도");
    }

    @Test
    public void 한도초과_송금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> balanceValidatorService
                .validateTransfer(testAccount.getId(), BigDecimal.valueOf(30000001)))
                .satisfies(ex -> System.out.println(ex.getMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일 한도");
    }
}
