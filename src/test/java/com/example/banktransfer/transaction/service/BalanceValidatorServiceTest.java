package com.example.banktransfer.transaction.service;

import com.example.banktransfer.global.annotation.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class BalanceValidatorServiceTest {
    @Autowired
    private BalanceValidatorService balanceValidatorService;

    @Sql(scripts = "classpath:rich-account-create.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    public void 잔액검증() {
//        assertThatThrownBy(() -> balanceValidatorService
//                .validateWithdrawal(1L, BigDecimal.valueOf(3000)))
//                .satisfies(ex -> System.out.println(ex.getMessage()))
//                .isInstanceOf(RuntimeException.class);

        assertThat(balanceValidatorService.validateWithdrawal(1L, BigDecimal.valueOf(3000)))
                .isEqualTo(true);
    }
}