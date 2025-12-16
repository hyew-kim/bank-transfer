package com.example.banktransfer.account.domain;

import com.example.banktransfer.account.AccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 14)
    private String accountNumber;

    @Column(length = 3)
    private String bankCode = "777"; //TODO: 유저가 여러 은행 계좌를 가지도록 확장

    @Column(name = "holder_name", length = 50)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Getter
    private AccountStatus status; // ACTIVE, DORMANT, CLOSED

    @Column(precision = 15, scale = 2)
    private BigDecimal balance =  BigDecimal.ZERO;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Account(String holderName) {
        String generalFinance = "00";

        this.holderName = holderName;
        accountNumber = String.join("", bankCode, generalFinance, String.format("%09d", generateSequence()));
        status = AccountStatus.ACTIVE;
    }

    private long generateSequence() {
        // DB sequence (AUTO_INCREMENT) 활용
        return System.nanoTime() % 999999999;
    }

    public void changeAccountStatus(AccountStatus tobeStatus) {
        if (!status.getIsChangeable()) {
            throw new IllegalStateException("변경이 불가한 계좌입니다.");
        }

        status = tobeStatus;
    }
}
