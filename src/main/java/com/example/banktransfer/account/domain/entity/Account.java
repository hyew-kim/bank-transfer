package com.example.banktransfer.account.domain.entity;

import com.example.banktransfer.account.AccountStatus;
import com.example.banktransfer.account.exception.AccountClosedException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_user_bank_number",
                        columnNames = {"user_id", "bank_code", "account_number"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "account_number", nullable = false, length = 14)
    @Getter
    private String accountNumber;

    @Column(name = "bank_code", nullable = false, length = 3)
    @Getter
    private String bankCode;

    @Column(name = "user_id", nullable = false)
    @Getter
    private Long userId;

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

    @Version
    private Long version;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void validateRequiredFields() {
        if (userId == null) {
            throw new AccountClosedException.InvalidAccountException();
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new AccountClosedException.InvalidAccountException();
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new AccountClosedException.InvalidAccountNumberException();
        }
    }

    public void changeAccountStatus(AccountStatus tobeStatus) {
        if (!status.getIsChangeable()) {
            throw new AccountClosedException();
        }

        status = tobeStatus;
    }

    public void changeBalance(BigDecimal toBeBalance) {
        this.balance = toBeBalance;
    }
}
