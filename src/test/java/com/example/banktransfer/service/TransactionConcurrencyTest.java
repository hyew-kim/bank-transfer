package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import com.example.banktransfer.global.fixture.AccountFixture;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class TransactionConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountRepository accountRepository;

    private static final BigDecimal INIT_BALANCE = BigDecimal.valueOf(10000);
    private static final String ACCOUNT_HOLDER = "tester";

    @BeforeEach
    void setUpAccount() {
        Account testAccount = AccountFixture.createAccountWithBalance(ACCOUNT_HOLDER, INIT_BALANCE);
        accountRepository.save(testAccount);
    }

    @Test
    public void 동시_입금은_모두_반영된다() throws InterruptedException {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        int threadCount = 5;
        BigDecimal depositAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CountDownLatch proceedLatch = new CountDownLatch(1);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        readyLatch.countDown();
                        proceedLatch.await(30, TimeUnit.SECONDS);
                        transactionService.deposit(
                                testAccount.getId(),
                                new MoneyRequest(depositAmount, "동시 입금")
                        );
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드가 중복 체크 지점까지 도착")
                    .isTrue();

            proceedLatch.countDown();

            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드 작업 완료")
                    .isTrue();
        } finally {
            executorService.shutdown();
        }

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo(INIT_BALANCE.add(depositAmount.multiply(BigDecimal.valueOf(threadCount))));

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("잔액: " + updatedAccount.getBalance());
    }

    @Test
    public void 동시_출금은_모두_반영된다() throws InterruptedException {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        int threadCount = 5;
        BigDecimal withdrawAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch proceedLatch = new CountDownLatch(1);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        readyLatch.countDown();
                        proceedLatch.await(30, TimeUnit.SECONDS);
                        transactionService.withdraw(
                                testAccount.getId(),
                                new MoneyRequest(withdrawAmount, "동시 출금")
                        );
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드가 중복 체크 지점까지 도착")
                    .isTrue();
            proceedLatch.countDown();
            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드 작업 완료")
                    .isTrue();
        } finally {
            executorService.shutdown();
        }

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo(INIT_BALANCE.subtract(withdrawAmount.multiply(BigDecimal.valueOf(threadCount))));

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("잔액: " + updatedAccount.getBalance());
    }

    @Test
    public void 동시_이체는_모두_반영된다() throws InterruptedException {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        Account toAccount = AccountFixture.createAccountWithBalance("receiver", INIT_BALANCE);
        accountRepository.save(toAccount);

        int threadCount = 5;
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch proceedLatch = new CountDownLatch(1);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        readyLatch.countDown();
                        proceedLatch.await(30, TimeUnit.SECONDS);
                        transactionService.transfer(
                                testAccount.getId(),
                                new TransferRequest(toAccount.getId(), transferAmount, "동시 이체")
                        );
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            assertThat(readyLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드가 중복 체크 지점까지 도착")
                    .isTrue();

            proceedLatch.countDown();

            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드 작업 완료")
                    .isTrue();
        } finally {
            executorService.shutdown();
        }

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));
        Account updatedToAccount = accountRepository
                .findByHolderName("receiver")
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal fee = transferAmount.multiply(TransactionType.TRANSFER.getFeeRate());
        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo(INIT_BALANCE
                        .subtract(transferAmount.multiply(BigDecimal.valueOf(threadCount)))
                        .subtract(fee.multiply(BigDecimal.valueOf(threadCount))));
        assertThat(updatedToAccount.getBalance())
                .isEqualByComparingTo(INIT_BALANCE.add(transferAmount.multiply(BigDecimal.valueOf(threadCount))));

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("잔액: " + updatedAccount.getBalance());
        System.out.println("receiver 잔액: " + updatedToAccount.getBalance());
    }

    @Test
    public void 동시_출금은_모두_반영된다_한도체크() throws InterruptedException {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal initLimit = testAccount.getDailyLimitOfWithdrawal();

        int threadCount = 5;
        BigDecimal withdrawAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch proceedLatch = new CountDownLatch(1);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        readyLatch.countDown();
                        proceedLatch.await(30, TimeUnit.SECONDS);
                        transactionService.withdraw(
                                testAccount.getId(),
                                new MoneyRequest(withdrawAmount, "동시 출금")
                        );
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드가 중복 체크 지점까지 도착")
                    .isTrue();
            proceedLatch.countDown();
            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드 작업 완료")
                    .isTrue();
        } finally {
            executorService.shutdown();
        }

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThat(updatedAccount.getDailyLimitOfWithdrawal())
                .isEqualByComparingTo(BigDecimal.ZERO.max(initLimit.subtract(withdrawAmount.multiply(BigDecimal.valueOf(threadCount)))));

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("한도: " + updatedAccount.getDailyLimitOfWithdrawal());
    }

    @Test
    public void 동시_이체는_모두_반영된다_한도체크() throws InterruptedException {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal initLimit = testAccount.getDailyLimitOfTransfer();

        Account toAccount = AccountFixture.createAccountWithBalance("receiver", INIT_BALANCE);
        accountRepository.save(toAccount);

        int threadCount = 5;
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch proceedLatch = new CountDownLatch(1);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        readyLatch.countDown();
                        proceedLatch.await(30, TimeUnit.SECONDS);
                        transactionService.transfer(
                                testAccount.getId(),
                                new TransferRequest(toAccount.getId(), transferAmount, "동시 이체")
                        );
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            assertThat(readyLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드가 중복 체크 지점까지 도착")
                    .isTrue();

            proceedLatch.countDown();

            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("모든 스레드 작업 완료")
                    .isTrue();
        } finally {
            executorService.shutdown();
        }

        Account updatedAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        BigDecimal fee = transferAmount.multiply(TransactionType.TRANSFER.getFeeRate());
        assertThat(updatedAccount.getDailyLimitOfTransfer())
                .isEqualByComparingTo(BigDecimal.ZERO.max(initLimit
                        .subtract(transferAmount.multiply(BigDecimal.valueOf(threadCount)))
                        .subtract(fee.multiply(BigDecimal.valueOf(threadCount)))));

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("한도: " + updatedAccount.getDailyLimitOfTransfer());
    }
}
