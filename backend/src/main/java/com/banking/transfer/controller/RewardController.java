package com.banking.transfer.controller;

import com.banking.transfer.dto.RedeemRequest;
import com.banking.transfer.dto.RedeemResponse;
import com.banking.transfer.dto.RewardResponse;
import com.banking.transfer.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RewardController {

    private final RewardService rewardService;

    /**
     * GET /api/v1/rewards/{accountId}
     * Returns the total reward points and full reward history.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<RewardResponse> getRewards(@PathVariable String accountId) {
        RewardResponse response = rewardService.getRewardsForAccount(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/rewards/{accountId}/redeem
     * Redeems reward points as cashback (1 point = ₹1).
     * Minimum redemption: 10 points.
     */
    @PostMapping("/{accountId}/redeem")
    public ResponseEntity<RedeemResponse> redeemPoints(
            @PathVariable String accountId,
            @Valid @RequestBody RedeemRequest request) {
        RedeemResponse response = rewardService.redeemPoints(accountId, request.getPointsToRedeem());
        return ResponseEntity.ok(response);
    }
}
