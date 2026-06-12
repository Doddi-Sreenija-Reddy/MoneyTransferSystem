package com.banking.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemResponse {

    /** Number of points that were redeemed. */
    private int pointsRedeemed;

    /** Cashback credited to account (1 point = ₹1). */
    private BigDecimal cashbackAmount;

    /** Updated account balance after cashback. */
    private BigDecimal newBalance;

    /** Remaining reward points after redemption. */
    private int remainingPoints;

    /** Human-readable success message. */
    private String message;
}
