package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.exception.AccountOwnershipException;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.account.service.AccountValidatorService;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import com.example.banktransfer.transaction.exception.DailyLimitExceededException;
import com.example.banktransfer.global.fixture.AccountFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class AccountValidatorServiceTest extends BaseIntegrationTest {
    @Autowired
    private AccountValidatorService accountValidatorService;
    @Autowired
    private AccountRepository accountRepository;

    final String DEFAULT_TEST_NAME = "balance-tester";
    @Test
    public void 잔액이_충분한_계좌_출금검증_성공() {
        Account testAccount = AccountFixture.createRichAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatCode(() -> accountValidatorService
                .validateWithdrawal(testAccount, BigDecimal.valueOf(3000)))
                .doesNotThrowAnyException();
    }

    @Test
    public void 잔액부족_출금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> accountValidatorService
                .validateWithdrawal(testAccount, BigDecimal.valueOf(888888)))
                .isInstanceOf(AccountOwnershipException.InsufficientBalanceException.class);
    }

    @Test
    public void 잔액부족_송금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> accountValidatorService
                .validateTransfer(testAccount, BigDecimal.valueOf(1000000)))
                .isInstanceOf(AccountOwnershipException.InsufficientBalanceException.class);
    }

    @Test
    public void 한도초과_출금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> accountValidatorService
                .validateWithdrawal(testAccount,BigDecimal.valueOf(10000001)))
                .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    public void 한도초과_송금() {
        Account testAccount = AccountFixture.createPoorAccount(DEFAULT_TEST_NAME);
        accountRepository.save(testAccount);

        assertThatThrownBy(() -> accountValidatorService
                .validateTransfer(testAccount, BigDecimal.valueOf(30000001)))
                .isInstanceOf(DailyLimitExceededException.class);
    }
}
