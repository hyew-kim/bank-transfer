package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.exception.AccountException;
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
    void 동일한_사용자의_연속_계좌등록_1개만_성공() throws InterruptedException {
        String holderName = "Junit-tester";
        Long userId = 1L;
        String bankCode = "777";
        String accountNumber = "77700000000010";
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);    // Phase 1: 준비
        CountDownLatch proceedLatch = new CountDownLatch(1);        // Phase 2: 출발
        CountDownLatch doneLatch = new CountDownLatch(threadCount);     // Phase 3: 완료
        AtomicInteger inProgressRejected = new AtomicInteger(0);
        AtomicInteger otherError = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    proceedLatch.await(10, TimeUnit.SECONDS);

                    try {
                        accountService.createAccount(CreateAccountRequest.of(
                                userId,
                                bankCode,
                                accountNumber,
                                holderName
                        ));
                        successCount.incrementAndGet(); // 예외 없이 끝난 경우
                    } catch (AccountException.LinkingInProgressException ex) {
                        inProgressRejected.incrementAndGet();
                    } catch (Exception ex) {
                        otherError.incrementAndGet();
                        ex.printStackTrace();
                    }

                } catch (InterruptedException ex) {
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

        System.out.println("success = " + successCount.get());
        System.out.println("inProgressRejected = " + inProgressRejected.get());
        System.out.println("otherError = " + otherError.get());
        System.out.println("accountCount = " + accountRepository.count());

        assertThat(accountRepository.count())
                .as("생성된 계좌 수")
                .isEqualTo(1);

        assertThat(successCount.get())
                .as("성공 횟수")
                .isEqualTo(1);

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("성공: " + successCount.get());
        System.out.println("생성된 계좌: " + accountRepository.count());
    }


}
