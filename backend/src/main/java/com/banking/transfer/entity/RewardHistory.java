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

    /** The account that earned the reward (the sender). */
    @Column(nullable = false)
    private String accountId;

    /** The transaction that triggered this reward. */
    @Column(nullable = false, unique = true)
    private String transactionId;

    /** Reward points awarded: floor(transactionAmount / 100). */
    @Column(nullable = false)
    private int pointsEarned;

    /** Original transfer amount for traceability. */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal transactionAmount;

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
    }
}
