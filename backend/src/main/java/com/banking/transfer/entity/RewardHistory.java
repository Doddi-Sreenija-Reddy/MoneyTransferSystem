package com.banking.transfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reward_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardHistory {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    /** The account that earned or redeemed the reward. */
    @Column(nullable = false)
    private String accountId;

    /**
     * The transaction that triggered this reward.
     * Null for REDEEMED entries (no triggering transfer).
     */
    @Column(unique = true)
    private String transactionId;

    /**
     * Points earned (positive for EARNED, negative for REDEEMED).
     * SUM of all rows = net available points.
     */
    @Column(nullable = false)
    private int pointsEarned;

    /** Original transfer amount (for EARNED rows) or cashback value (for REDEEMED rows). */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal transactionAmount;

    /** Whether these points were earned from a transfer or redeemed for cashback. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RewardType type = RewardType.EARNED;

    @Column(nullable = false)
    private LocalDateTime awardedOn;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.awardedOn == null) {
            this.awardedOn = LocalDateTime.now();
        }
        if (this.type == null) {
            this.type = RewardType.EARNED;
        }
    }
}

