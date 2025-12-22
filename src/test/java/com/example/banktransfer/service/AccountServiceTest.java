package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.dto.AccountResponse;
import com.example.banktransfer.account.domain.dto.CreateAccountRequest;
import com.example.banktransfer.account.service.AccountService;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.progress.ProgressRecorder;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
@IntegrationTest
public class AccountServiceTest extends BaseIntegrationTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @MockitoBean
    private ProgressRecorder progressRecorder;

    @Test
    public void 한명의_계좌개설_성공() {
        String holderName = "Junit-tester";
        CreateAccountRequest request = CreateAccountRequest.of(holderName);

        accountService.createAccount(request);
        Account account = accountRepository.findByHolderName(holderName).orElse(null);

        assertThat(account)
                .isNotNull();
    }

    @Test
    public void 계좌보유_유저의_계좌해지_성공() {
        String holderName = "Junit-tester";
        CreateAccountRequest request = CreateAccountRequest.of(holderName);

        accountService.createAccount(request);
        Account account = accountRepository
                .findByHolderName(holderName)
                .orElse(null);

        assertThat(account)
                .as("계좌 개설 실패")
                .isNotNull();

        accountService.closeAccount(account.getId());
        assertThat(accountRepository.findByHolderName(holderName).orElse(null))
                .as("계좌 해지 실패 ")
                .extracting(Account::getStatus)
                .isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    public void 동일_계좌_해지_요청_중복_차단() {
        String holderName = "Junit-tester";
        CreateAccountRequest request = CreateAccountRequest.of(holderName);

        accountService.createAccount(request);
        Account account = accountRepository
                .findByHolderName(holderName)
                .orElseThrow(() -> new RuntimeException("Not found"));

        String progressKey = "account:close:" + account.getId();
        when(progressRecorder.getStatus(progressKey))
                .thenReturn(com.example.banktransfer.global.progress.ProgressStatus.PROCESSING);

        assertThatThrownBy(() -> accountService.closeAccount(account.getId()))
                .satisfies(System.out::println)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 해지 진행 중");
    }

    @Test
    public void 동일_계좌_개설_요청_중복_차단() {
        String holderName = "Junit-tester";
        CreateAccountRequest request = CreateAccountRequest.of(holderName);

        String progressKey = "account:create:" + request.holderName();
        when(progressRecorder.getStatus(progressKey))
                .thenReturn(com.example.banktransfer.global.progress.ProgressStatus.PROCESSING);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .satisfies(System.out::println)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 개설 진행 중");
    }

    @Test
    public void 예금주명으로_계좌조회_성공() {
        String holderName = "Junit-tester";
        CreateAccountRequest request = CreateAccountRequest.of(holderName);

        accountService.createAccount(request);
        AccountResponse account = accountService.searchAccount(holderName);

        assertThat(account)
                .isNotNull();
    }
}
