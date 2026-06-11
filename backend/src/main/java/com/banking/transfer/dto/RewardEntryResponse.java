package com.banking.transfer.dto;

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
public class RewardEntryResponse {

    private String id;
    private String transactionId;
    private int pointsEarned;
    private BigDecimal transactionAmount;
    private LocalDateTime awardedOn;
}
