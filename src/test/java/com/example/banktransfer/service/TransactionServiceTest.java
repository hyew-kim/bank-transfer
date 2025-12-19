package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.fixture.AccountFixture;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.repository.TransactionRepository;
import com.example.banktransfer.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class TransactionServiceTest {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;

    final BigDecimal INIT_BALANCE = BigDecimal.valueOf(10000);
    final String ACCOUNT_HOLDER = "tester";

    @BeforeEach
    void setUpAccount() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        Account testAccount = AccountFixture.createAccountWithBalance(ACCOUNT_HOLDER, INIT_BALANCE);
        accountRepository.save(testAccount);
    }

    @Test
    public void 입금성공() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        transactionService.deposit(
                testAccount.getId(),
                new MoneyRequest(depositAmount, "test 입금")
        );

        assertThat(transactionRepository.count())
                .as("거래 내역 생성 실패")
                .isEqualTo(1);

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThat(updatedAccount.getBalance())
                .as("입금 로직 실패")
                .isEqualByComparingTo(INIT_BALANCE.add(depositAmount));
    }

    @Test
    public void 출금성공() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));
        BigDecimal withdrawAmount = BigDecimal.valueOf(550);

        transactionService.withdraw(
                testAccount.getId(),
                new MoneyRequest(withdrawAmount, "test 출금")
        );

        assertThat(transactionRepository.count())
                .as("거래 내역 생성 실패")
                .isEqualTo(1);

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThat(updatedAccount.getBalance())
                .as("출금 로직 실패")
                .isEqualByComparingTo(INIT_BALANCE.subtract(withdrawAmount));
    }

    @Test
    public void 이체성공() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        Account toAccount = AccountFixture.createAccountWithBalance("테스터어", INIT_BALANCE);
        accountRepository.save(toAccount);

        BigDecimal transferAmount = BigDecimal.valueOf(550);

        transactionService.transfer(
                testAccount.getId(),
                new TransferRequest(toAccount.getId(), transferAmount, "test 이체")
        );

        assertThat(transactionRepository.count())
                .as("거래 내역 생성 실패")
                .isEqualTo(1);

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        Account updatedToAccount = accountRepository
                .findByHolderName("테스터어")
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal fee = transferAmount.multiply(TransactionType.TRANSFER.getFeeRate());
        assertThat(updatedAccount.getBalance())
                .as("이체 로직 실패 - 계좌 소유주 잔액 정합성")
                .isEqualByComparingTo(INIT_BALANCE.subtract(transferAmount).subtract(fee));

        assertThat(updatedToAccount.getBalance())
                .as("이체 로직 실패 - 이체 대상 잔액 정합성")
                .isEqualByComparingTo(INIT_BALANCE.add(transferAmount));
    }
}
