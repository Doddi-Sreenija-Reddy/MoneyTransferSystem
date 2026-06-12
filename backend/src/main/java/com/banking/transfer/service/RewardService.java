package com.banking.transfer.service;

import com.banking.transfer.dto.RedeemResponse;
import com.banking.transfer.dto.RewardEntryResponse;
import com.banking.transfer.dto.RewardResponse;
import com.banking.transfer.entity.Account;
import com.banking.transfer.entity.RewardHistory;
import com.banking.transfer.entity.RewardType;
import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.AccountNotActiveException;
import com.banking.transfer.repository.AccountRepository;
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
 * Earn rules (ALL must be met):
 *  1. Transaction status is SUCCESS
 *  2. Transaction amount > 100 (rupees)
 *  3. Sender and receiver are different accounts (not a self-transfer)
 *
 * Earn calculation:  points = floor(amount / 100)
 * Redeem rate:       1 point = ₹1 cashback (minimum 10 points per redemption)
 *
 * net available points = SUM(pointsEarned) across all EARNED and REDEEMED rows
 * (REDEEMED rows store negative values, so the SUM naturally gives net balance).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private static final BigDecimal REWARD_THRESHOLD   = new BigDecimal("100");
    private static final BigDecimal POINTS_PER_UNIT    = new BigDecimal("100");
    /** 1 point = ₹1 */
    private static final BigDecimal RUPEES_PER_POINT   = BigDecimal.ONE;
    private static final int        MIN_REDEEM_POINTS  = 10;

    private final RewardHistoryRepository rewardHistoryRepository;
    private final AccountRepository       accountRepository;

    // ─────────────────────────────────────────────────────────────
    // Earn
    // ─────────────────────────────────────────────────────────────

    /**
     * Evaluates whether the completed transaction is eligible for a reward,
     * and if so persists a RewardHistory record.
     */
    @Transactional
    public void evaluateAndGrantReward(TransactionLog txn) {

        if (txn.getStatus() != TransactionStatus.SUCCESS) {
            log.info("Reward skipped — transaction {} is not SUCCESS", txn.getId());
            return;
        }

        if (txn.getAmount().compareTo(REWARD_THRESHOLD) <= 0) {
            log.info("Reward skipped — amount {} ≤ 100 for transaction {}", txn.getAmount(), txn.getId());
            return;
        }

        if (txn.getFromAccountId().equals(txn.getToAccountId())) {
            log.info("Reward skipped — self-transfer for transaction {}", txn.getId());
            return;
        }

        if (rewardHistoryRepository.existsByTransactionId(txn.getId())) {
            log.warn("Reward already granted for transaction {}, skipping", txn.getId());
            return;
        }

        int points = txn.getAmount()
                .divide(POINTS_PER_UNIT, 0, RoundingMode.FLOOR)
                .intValue();

        RewardHistory reward = RewardHistory.builder()
                .accountId(txn.getFromAccountId())
                .transactionId(txn.getId())
                .pointsEarned(points)
                .transactionAmount(txn.getAmount())
                .type(RewardType.EARNED)
                .build();

        rewardHistoryRepository.save(reward);
        log.info("Reward granted: {} pts to account {} for txn {} (₹{})",
                points, txn.getFromAccountId(), txn.getId(), txn.getAmount());
    }

    // ─────────────────────────────────────────────────────────────
    // Query
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // Redeem  (1 point = ₹1)
    // ─────────────────────────────────────────────────────────────

    /**
     * Redeems reward points as cashback directly into the account balance.
     *
     * @param accountId      the account performing the redemption
     * @param pointsToRedeem number of points to redeem (must be ≥ 10)
     * @return RedeemResponse with cashback amount, new balance, remaining points
     */
    @Transactional
    public RedeemResponse redeemPoints(String accountId, int pointsToRedeem) {

        // Validate minimum redemption
        if (pointsToRedeem < MIN_REDEEM_POINTS) {
            throw new IllegalArgumentException(
                    "Minimum redemption is " + MIN_REDEEM_POINTS + " points");
        }

        // Fetch net available points
        int availablePoints = rewardHistoryRepository.sumPointsByAccountId(accountId);
        if (pointsToRedeem > availablePoints) {
            throw new IllegalArgumentException(
                    "Insufficient points. Available: " + availablePoints
                            + ", requested: " + pointsToRedeem);
        }

        // Fetch account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId));

        // Guard: account must be active
        if (!account.isActive()) {
            throw new AccountNotActiveException(
                    "Account " + accountId + " is not active and cannot redeem points");
        }

        // Compute cashback: 1 point = ₹1
        BigDecimal cashback = BigDecimal.valueOf(pointsToRedeem).multiply(RUPEES_PER_POINT);

        // Credit account balance
        account.credit(cashback);
        accountRepository.save(account);

        // Record redemption (negative points so net SUM decreases)
        RewardHistory redemption = RewardHistory.builder()
                .accountId(accountId)
                .transactionId(null)            // no transfer triggered this
                .pointsEarned(-pointsToRedeem)  // negative to reduce net balance
                .transactionAmount(cashback)
                .type(RewardType.REDEEMED)
                .build();

        rewardHistoryRepository.save(redemption);

        int remainingPoints = availablePoints - pointsToRedeem;

        log.info("Redeemed {} pts → ₹{} cashback for account {}. Remaining: {} pts",
                pointsToRedeem, cashback, accountId, remainingPoints);

        return RedeemResponse.builder()
                .pointsRedeemed(pointsToRedeem)
                .cashbackAmount(cashback)
                .newBalance(account.getBalance())
                .remainingPoints(remainingPoints)
                .message(pointsToRedeem + " points redeemed for ₹" + cashback + " cashback!")
                .build();
    }
}
