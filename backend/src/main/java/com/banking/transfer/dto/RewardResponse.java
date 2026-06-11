package com.banking.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardResponse {

    /** Total accumulated reward points for this account. */
    private int totalPoints;

    /** Full reward history, ordered newest-first. */
    private List<RewardEntryResponse> rewards;
}
