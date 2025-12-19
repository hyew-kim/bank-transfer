package com.example.banktransfer.account.domain.entity;

import com.example.banktransfer.account.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Account {
    private static final String GENERAL_FINANCE = "00";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 14)
    @Getter
    private String accountNumber;

    @Column(length = 3)
    @Builder.Default
    private String bankCode = "777"; //TODO: 유저가 여러 은행 계좌를 가지도록 확장

    @Column(name = "holder_name", length = 50)
    @Getter
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Getter
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    @Getter
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "daily_limit_of_withdrawal", precision = 15, scale = 2)
    @Builder.Default
    @Getter
    private BigDecimal dailyLimitOfWithdrawal = BigDecimal.valueOf(1_000_000);

    @Column(name = "daily_limit_of_transfer", precision = 15, scale = 2)
    @Builder.Default
    @Getter
    private BigDecimal dailyLimitOfTransfer = BigDecimal.valueOf(3_000_000);

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void generateAccountNumber() {
        String uniqueId = String.format("%09d", ThreadLocalRandom.current().nextInt(999999999));

        this.accountNumber = String.format("%s%s%s", this.bankCode, GENERAL_FINANCE, uniqueId);
    }

    public void changeAccountStatus(AccountStatus tobeStatus) {
        if (!status.getIsChangeable()) {
            throw new IllegalStateException("변경이 불가한 계좌입니다.");
        }

        status = tobeStatus;
    }

    public void changeBalance(BigDecimal toBeBalance) {
        this.balance = toBeBalance;
    }
}
