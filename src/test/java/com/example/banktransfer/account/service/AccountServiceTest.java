package com.example.banktransfer.account.service;

import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
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
    public void 동일한_사용자의_연속_계좌개설을_시도_한개만_성공() throws InterruptedException {
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

    @Test
    public void 계좌보유_유저의_계좌해지_성공() {
        String holderName = "Junit-tester";

        accountService.createAccount(holderName);
        Account account = accountRepository.findByHolderName(holderName);
        assertThat(account)
                .as("계좌 개설 실패")
                .isNotNull();

        accountService.closeAccount(holderName);
        assertThat(accountRepository.findByHolderName(holderName))
                .as("계좌 해지 실패 ")
                .extracting(Account::getStatus)
                .isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    public void 예금주명으로_계좌조회_성공() {
        String holderName = "Junit-tester";

        accountService.createAccount(holderName);
        Account account = accountService.searchAccount(holderName);

        assertThat(account)
                .isNotNull();
    }
}
