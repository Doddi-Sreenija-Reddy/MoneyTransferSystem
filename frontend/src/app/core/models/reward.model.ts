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
