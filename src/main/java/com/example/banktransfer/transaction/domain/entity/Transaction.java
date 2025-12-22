package com.example.banktransfer.transaction.domain.entity;

import com.example.banktransfer.account.domain.entity.Account;
import com.example.banktransfer.transaction.TransactionStatus;
import com.example.banktransfer.transaction.TransactionType;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false, length = 20)
    @Getter
    private String transactionId;

    @Getter
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(precision = 15, scale = 2)
    @Getter
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    @Getter
    private BigDecimal fee;

    @Column(precision = 5, scale = 4)
    private BigDecimal feeRate;

    @Column(length = 100)
    @Getter
    private String description;

    @Column(name = "to_account_number", length = 14)
    @Getter
    private String toAccountNumber;

    @Column(name = "to_holder_name", length = 50)
    private String toHolderName;

    @Column(name = "from_account_number", length = 14)
    @Getter
    private String fromAccountNumber;

    @Column(name = "from_holder_name", length = 50)
    private String fromHolderName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Getter
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    @Getter
    private Account account;

    @CreatedDate
    @Column(updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @Setter
    @Getter
    private String failureReason;

    @PrePersist
    protected void generateTid() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int suffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000);
        this.transactionId = String.format("TRX%s%03d", timestamp, suffix);
    }

    public void setStatus(TransactionStatus status) {
        if (TransactionStatus.SUCCESS == status) {
            this.completedAt = LocalDateTime.now();
        }
        this.status = status;
    }
}
