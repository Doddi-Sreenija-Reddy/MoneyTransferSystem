package com.banking.transfer.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemRequest {

    /** Number of points to redeem. Minimum 10 points (= ₹10). */
    @Min(value = 10, message = "Minimum redemption is 10 points")
    private int pointsToRedeem;
}
