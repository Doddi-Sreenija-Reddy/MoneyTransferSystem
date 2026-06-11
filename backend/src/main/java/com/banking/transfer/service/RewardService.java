package com.banking.transfer.service;

import com.banking.transfer.dto.RewardEntryResponse;
import com.banking.transfer.dto.RewardResponse;
import com.banking.transfer.entity.RewardHistory;
import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.repository.RewardHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reward Module Service.
 *
 * Eligibility rules (ALL must be met):
 *  1. Transaction status is SUCCESS
 *  2. Transaction amount > 100 (rupees)
 *  3. Sender and receiver are different accounts (not a self-transfer)
 *
 * Reward calculation:
 *  points = floor(amount / 100)
 *
 * Only the sender (fromAccountId) earns reward points.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private static final BigDecimal REWARD_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal POINTS_PER_UNIT  = new BigDecimal("100");

    private final RewardHistoryRepository rewardHistoryRepository;

    /**
     * Evaluates whether the completed transaction is eligible for a reward,
     * and if so persists a RewardHistory record.
     *
     * This is called within the same transaction as the successful transfer
     * so both the transfer and the reward are committed atomically.
     *
     * @param txn the saved, successful TransactionLog
     */
    @Transactional
    public void evaluateAndGrantReward(TransactionLog txn) {

        // Rule 1: status must be SUCCESS
        if (txn.getStatus() != TransactionStatus.SUCCESS) {
            log.info("Reward skipped — transaction {} is not SUCCESS", txn.getId());
            return;
        }

        // Rule 2: amount must be strictly greater than 100
        if (txn.getAmount().compareTo(REWARD_THRESHOLD) <= 0) {
            log.info("Reward skipped — amount {} ≤ 100 for transaction {}", txn.getAmount(), txn.getId());
            return;
        }

        // Rule 3 & 4: sender and receiver must be different (not self-transfer)
        if (txn.getFromAccountId().equals(txn.getToAccountId())) {
            log.info("Reward skipped — self-transfer detected for transaction {}", txn.getId());
            return;
        }

        // Idempotency guard — never double-reward the same transaction
        if (rewardHistoryRepository.existsByTransactionId(txn.getId())) {
            log.warn("Reward already granted for transaction {}, skipping duplicate", txn.getId());
            return;
        }

        // Reward calculation: floor(amount / 100)
        int points = txn.getAmount()
                .divide(POINTS_PER_UNIT, 0, RoundingMode.FLOOR)
                .intValue();

        RewardHistory reward = RewardHistory.builder()
                .accountId(txn.getFromAccountId())
                .transactionId(txn.getId())
                .pointsEarned(points)
                .transactionAmount(txn.getAmount())
                .build();

        rewardHistoryRepository.save(reward);

        log.info("Reward granted: {} points to account {} for transaction {} (amount: {})",
                points, txn.getFromAccountId(), txn.getId(), txn.getAmount());
    }

    /**
     * Retrieves all reward history and total points for a given account.
     *
     * @param accountId the account to query
     * @return RewardResponse with totalPoints and list of entries
     */
    @Transactional(readOnly = true)
    public RewardResponse getRewardsForAccount(String accountId) {
        int totalPoints = rewardHistoryRepository.sumPointsByAccountId(accountId);

        List<RewardEntryResponse> entries = rewardHistoryRepository
                .findByAccountIdOrderByAwardedOnDesc(accountId)
                .stream()
                .map(r -> RewardEntryResponse.builder()
                        .id(r.getId())
                        .transactionId(r.getTransactionId())
                        .pointsEarned(r.getPointsEarned())
                        .transactionAmount(r.getTransactionAmount())
                        .awardedOn(r.getAwardedOn())
                        .build())
                .toList();

        return RewardResponse.builder()
                .totalPoints(totalPoints)
                .rewards(entries)
                .build();
    }
}
