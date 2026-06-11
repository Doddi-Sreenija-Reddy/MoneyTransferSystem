package com.banking.transfer.service;

import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for persisting transaction logs. Uses REQUIRES_NEW propagation
 * for failed transactions so they are committed even when the parent
 * transfer transaction rolls back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    /**
     * Saves a failed transaction in a separate transaction. This ensures
     * the failed log is persisted even when the calling transfer transaction
     * is rolled back due to an exception.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransaction(String fromAccountId, String toAccountId, BigDecimal amount,
                                      String failureReason, String idempotencyKey) {
        TransactionLog failedLog = TransactionLog.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .status(TransactionStatus.FAILED)
                .failureReason(failureReason)
                .idempotencyKey(idempotencyKey)
                .build();

        transactionLogRepository.save(failedLog);
        log.info("Recorded failed transaction for idempotency key: {}, reason: {}", idempotencyKey, failureReason);
    }
}
