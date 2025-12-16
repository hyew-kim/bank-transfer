package com.example.banktransfer.account.service;

import com.example.banktransfer.account.domain.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = "local")
@Transactional
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void 한명의_계좌개설_성공() {
        String holderName = "Junit-tester";

        accountService.createAccount(holderName);

        Account account = accountRepository.findByHolderName(holderName);

        assertThat(account)
                .isNotNull();
    }

    @Test
    public void 동일한_사용자의_연속_계좌개설_실패() throws InterruptedException {
        int requestCount = 10;
        int userCount = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch countDownLatch = new CountDownLatch(userCount);

        for (int i = 0; i < requestCount; i++) {
            String holderName = "Junit-tester" + "_" + (userCount % requestCount);

            executorService.execute(() -> {
                try {
                    accountService.createAccount(holderName);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        assertThat(accountRepository.count())
                .isEqualTo(userCount);
    }
}
