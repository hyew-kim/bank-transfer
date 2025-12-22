package com.example.banktransfer.service;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.exception.AccountClosedException;
import com.example.banktransfer.account.exception.AccountOwnershipException;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.config.BaseIntegrationTest;
import com.example.banktransfer.global.fixture.AccountFixture;
import com.example.banktransfer.transaction.exception.InvalidInputException;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import com.example.banktransfer.transaction.domain.dto.MoneyRequest;
import com.example.banktransfer.transaction.domain.dto.TransactionResponse;
import com.example.banktransfer.transaction.domain.dto.TransferRequest;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.repository.TransactionRepository;
import com.example.banktransfer.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
public class TransactionServiceTest extends BaseIntegrationTest {
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

    @Test
    public void 계좌별_거래조회_성공() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        transactionService.deposit(testAccount.getId(), new MoneyRequest(BigDecimal.valueOf(10000), "test 입금"));

        transactionService.withdraw(testAccount.getId(), new MoneyRequest(BigDecimal.valueOf(3000), "test 출금"));

        List<TransactionResponse> transactions = transactionService.getAccountTransactions(testAccount.getId());

        assertThat(transactions)
                .hasSize(2);
        assertThat(transactions.get(0).type())
                .isEqualTo(TransactionType.WITHDRAW);
        assertThat(transactions.get(1).type())
                .isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    public void 입금실패_거래기록이_남는다() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThatThrownBy(() -> transactionService.deposit(
                testAccount.getId(),
                new MoneyRequest(BigDecimal.valueOf(-1), "잘못된 입금")
        )).isInstanceOf(InvalidInputException.class);

        List<Transaction> failed = transactionRepository
                .findByAccountIdAndStatusOrderByIdDesc(testAccount.getId(), TransactionStatus.FAILED)
                .orElse(List.of());

        assertThat(failed)
                .hasSize(1);
    }

    @Test
    public void 출금실패_거래기록이_남는다() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThatThrownBy(() -> transactionService.withdraw(
                testAccount.getId(),
                new MoneyRequest(INIT_BALANCE.add(BigDecimal.ONE), "잔액 부족 출금")
        )).isInstanceOf(AccountOwnershipException.InsufficientBalanceException.class);

        List<Transaction> failed = transactionRepository
                .findByAccountIdAndStatusOrderByIdDesc(testAccount.getId(), TransactionStatus.FAILED)
                .orElse(List.of());

        assertThat(failed)
                .hasSize(1);
    }

    @Test
    public void 이체실패_거래기록이_남는다_수취계좌없음() {
        Account testAccount = accountRepository
                .findByHolderName(ACCOUNT_HOLDER)
                .orElseThrow(() -> new RuntimeException("Not found"));

        assertThatThrownBy(() -> transactionService.transfer(
                testAccount.getId(),
                new TransferRequest(9999L, BigDecimal.valueOf(100), "수취계좌 없음")
        )).isInstanceOf(AccountClosedException.InvalidAccountException.class);

        List<Transaction> failed = transactionRepository
                .findByAccountIdAndStatusOrderByIdDesc(testAccount.getId(), TransactionStatus.FAILED)
                .orElse(List.of());

        assertThat(failed)
                .hasSize(1);
    }
}
