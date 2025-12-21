package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.account.service.AccountService;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class AccountConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void 동일한_사용자의_연속_계좌개설_한개만_성공() throws InterruptedException {
        String holderName = "Junit-tester";
        int threadCount = 100;  // 100개 동시 요청

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);    // Phase 1: 준비
        CountDownLatch proceedLatch = new CountDownLatch(1);        // Phase 2: 출발
        CountDownLatch doneLatch = new CountDownLatch(threadCount);     // Phase 3: 완료

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    proceedLatch.await(10, TimeUnit.SECONDS);
                    accountService.createAccount(CreateAccountRequest.of(holderName));
                    successCount.incrementAndGet();
                } catch (InterruptedException ex) {
                    // 중복 예외는 예상됨
                    failCount.incrementAndGet();
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS))
                .as("모든 스레드가 중복 체크 지점까지 도착")
                .isTrue();

        proceedLatch.countDown();

        assertThat(doneLatch.await(10, TimeUnit.SECONDS))
                .as("모든 스레드 작업 완료")
                .isTrue();

        executor.shutdown();

        assertThat(accountRepository.count())
                .as("생성된 계좌 수")
                .isEqualTo(1);

        assertThat(successCount.get())
                .as("성공 횟수")
                .isEqualTo(1);

        assertThat(failCount.get())
                .as("실패 횟수 (중복 예외)")
                .isEqualTo(99);

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("생성된 계좌: " + accountRepository.count());
    }

}
