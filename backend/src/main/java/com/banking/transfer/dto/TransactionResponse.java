package com.banking.transfer.dto;

import com.banking.transfer.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String id;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String failureReason;
    private LocalDateTime createdOn;
    private String type; // DEBIT or CREDIT
}
