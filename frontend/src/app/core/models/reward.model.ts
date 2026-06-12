export interface RewardEntry {
  id: string;
  transactionId: string;
  pointsEarned: number;
  transactionAmount: number;
  awardedOn: string;
}

export interface RewardResponse {
  totalPoints: number;
  rewards: RewardEntry[];
}

export interface RedeemRequest {
  pointsToRedeem: number;
}

export interface RedeemResponse {
  pointsRedeemed: number;
  cashbackAmount: number;
  newBalance: number;
  remainingPoints: number;
  message: string;
}
