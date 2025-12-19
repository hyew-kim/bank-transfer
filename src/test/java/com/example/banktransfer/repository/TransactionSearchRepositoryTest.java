package com.example.banktransfer.repository;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.account.repository.AccountRepository;
import com.example.banktransfer.global.annotation.IntegrationTest;
import com.example.banktransfer.global.fixture.AccountFixture;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.global.fixture.TransactionFixture;
import com.example.banktransfer.transaction.domain.entity.Transaction;
import com.example.banktransfer.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class TransactionSearchRepositoryTest {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

    private Long accountId;
    private String olderTransactionId;
    private String latestTransactionId;

    @BeforeEach
    void setUp() throws InterruptedException {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        Account account = AccountFixture.createAccountWithBalance("repo-tester", BigDecimal.valueOf(1000));
        accountRepository.save(account);

        Transaction first = TransactionFixture.createDepositTransaction(
                account, BigDecimal.valueOf(100), "first deposit"
        );
        transactionRepository.save(first);
        Thread.sleep(1100);

        Transaction second = TransactionFixture.createDepositTransaction(
                account, BigDecimal.valueOf(200), "second deposit"
        );
        transactionRepository.save(second);
        Thread.sleep(1100);

        Account otherAccount = AccountFixture.createAccountWithBalance("other-tester", BigDecimal.valueOf(500));
        accountRepository.save(otherAccount);
        Transaction otherTransaction = TransactionFixture.createDepositTransaction(
                otherAccount, BigDecimal.valueOf(50), "other deposit"
        );
        transactionRepository.save(otherTransaction);

        accountId = account.getId();
        olderTransactionId = first.getTransactionId();
        latestTransactionId = second.getTransactionId();
    }

    @Test
    void 계좌별_최신순_거래목록조회() {
        Optional<List<Transaction>> result = transactionRepository
                .findByAccountIdAndStatusOrderByIdDesc(accountId, TransactionStatus.SUCCESS);

        assertThat(result)
                .isPresent();

        List<Transaction> transactions = result.get();

        assertThat(transactions)
                .hasSize(2);
        assertThat(transactions.get(0).getTransactionId())
                .isEqualTo(latestTransactionId);
        assertThat(transactions.get(1).getTransactionId())
                .isEqualTo(olderTransactionId);
    }

}
