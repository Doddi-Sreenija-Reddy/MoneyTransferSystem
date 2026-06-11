package com.banking.transfer.service;

import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.repository.TransactionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransactionLogServiceTest {

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TransactionLogService transactionLogService;

    @Test
    void saveFailedTransaction_ShouldSaveLogWithFailedStatus() {
        String fromAccount = "ACC-1";
        String toAccount = "ACC-2";
        BigDecimal amount = new BigDecimal("100.00");
        String reason = "Insufficient funds";
        String idempotencyKey = "key-1";

        transactionLogService.saveFailedTransaction(fromAccount, toAccount, amount, reason, idempotencyKey);

        ArgumentCaptor<TransactionLog> captor = ArgumentCaptor.forClass(TransactionLog.class);
        verify(transactionLogRepository).save(captor.capture());

        TransactionLog capturedLog = captor.getValue();
        assertEquals(fromAccount, capturedLog.getFromAccountId());
        assertEquals(toAccount, capturedLog.getToAccountId());
        assertEquals(amount, capturedLog.getAmount());
        assertEquals(TransactionStatus.FAILED, capturedLog.getStatus());
        assertEquals(reason, capturedLog.getFailureReason());
        assertEquals(idempotencyKey, capturedLog.getIdempotencyKey());
    }
}
