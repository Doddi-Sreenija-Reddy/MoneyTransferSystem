package com.banking.transfer.repository;

import com.banking.transfer.entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, String> {

    /** Returns all reward records for a given account, newest first. */
    List<RewardHistory> findByAccountIdOrderByAwardedOnDesc(String accountId);

    /** Returns the total reward points accumulated by an account. */
    @Query("SELECT COALESCE(SUM(r.pointsEarned), 0) FROM RewardHistory r WHERE r.accountId = ?1")
    int sumPointsByAccountId(String accountId);

    /** Check if a reward already exists for a given transaction (idempotency guard). */
    boolean existsByTransactionId(String transactionId);
}
