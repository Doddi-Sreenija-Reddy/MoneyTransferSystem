import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth';
import { RewardService } from '../../core/services/reward';
import { StatementService } from '../../core/services/statement';
import { Navbar } from '../../shared/components/navbar/navbar';
import { RewardEntry, RewardResponse, RedeemResponse } from '../../core/models/reward.model';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
    MatInputModule,
    MatFormFieldModule,
    MatSnackBarModule,
    Navbar
  ],
  templateUrl: './rewards.html',
  styleUrl: './rewards.scss'
})
export class Rewards implements OnInit {
  rewardData: RewardResponse | null = null;
  displayedColumns: string[] = ['date', 'transactionId', 'amount', 'points'];
  loading = true;
  errorMessage = '';

  // Redemption state
  showRedeemPanel = false;
  /** Points the user wants to redeem (renamed from redeemPoints to avoid shadowing the service method) */
  redeemAmount = 10;
  redeeming = false;
  redeemSuccess: RedeemResponse | null = null;
  redeemError = '';

  // Date filter
  startDate = '';
  endDate = '';

  constructor(
    private authService: AuthService,
    private rewardService: RewardService,
    private statementService: StatementService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRewards();
  }

  loadRewards(): void {
    const accountId = this.authService.getAccountId();
    if (!accountId) {
      this.errorMessage = 'Unable to identify your account. Please log in again.';
      this.loading = false;
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.rewardService.getRewards(accountId).subscribe({
      next: (data) => {
        this.rewardData = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load reward data. Please try again.';
        this.loading = false;
        console.error('Error loading rewards:', err);
      }
    });
  }

  // ─── Redemption ───────────────────────────────────────────────

  toggleRedeemPanel(): void {
    this.showRedeemPanel = !this.showRedeemPanel;
    this.redeemSuccess = null;
    this.redeemError = '';
    // Default to 10 pts or the user's total if they have fewer (panel only opens when totalPoints >= 10)
    this.redeemAmount = Math.min(this.totalPoints, 10);
  }

  get maxRedeemable(): number {
    return this.totalPoints;
  }

  /** Live cashback preview: 1 point = ₹1 */
  get cashbackPreview(): number {
    const pts = Number(this.redeemAmount);
    return isNaN(pts) || pts < 0 ? 0 : pts;
  }

  /** True when the redeem input is in a valid state */
  get redeemInputValid(): boolean {
    const pts = Number(this.redeemAmount);
    return !isNaN(pts) && pts >= 10 && pts <= this.maxRedeemable;
  }

  submitRedeem(): void {
    const accountId = this.authService.getAccountId();
    if (!accountId) return;

    const pts = Number(this.redeemAmount);

    if (isNaN(pts) || pts < 10) {
      this.redeemError = 'Minimum redemption is 10 points.';
      return;
    }
    if (pts > this.totalPoints) {
      this.redeemError = `You only have ${this.totalPoints} points available.`;
      return;
    }

    this.redeeming = true;
    this.redeemError = '';
    this.redeemSuccess = null;

    this.rewardService.redeemPoints(accountId, { pointsToRedeem: pts }).subscribe({
      next: (res) => {
        this.redeemSuccess = res;
        this.redeeming = false;
        this.snackBar.open(
          `✅ ${res.pointsRedeemed} pts redeemed! ₹${res.cashbackAmount} added to your balance.`,
          'Close',
          { duration: 5000, panelClass: 'success-snack' }
        );
        // Reload rewards to reflect updated net points and add the REDEEMED row
        this.loadRewards();
      },
      error: (err) => {
        // err.error is the ErrorResponse from GlobalExceptionHandler
        this.redeemError = err?.error?.message || 'Redemption failed. Please try again.';
        this.redeeming = false;
      }
    });
  }

  // ─── Date Filter ──────────────────────────────────────────────

  get filteredEntries(): RewardEntry[] {
    let entries = this.rewardData?.rewards ?? [];
    if (this.startDate) {
      const start = new Date(this.startDate);
      entries = entries.filter(e => new Date(e.awardedOn) >= start);
    }
    if (this.endDate) {
      const end = new Date(this.endDate);
      end.setHours(23, 59, 59, 999);
      entries = entries.filter(e => new Date(e.awardedOn) <= end);
    }
    return entries;
  }

  get isFilterActive(): boolean {
    return !!this.startDate || !!this.endDate;
  }

  clearFilter(): void {
    this.startDate = '';
    this.endDate = '';
  }

  // ─── Downloads ────────────────────────────────────────────────

  downloadCSV(): void {
    const headers = ['Date & Time', 'Transaction ID', 'Transfer Amount (INR)', 'Points'];
    const rows = this.filteredEntries.map(e => [
      this.formatDate(e.awardedOn),
      e.transactionId ?? 'REDEMPTION',
      e.transactionAmount.toFixed(2),
      e.pointsEarned.toString()
    ]);
    this.statementService.downloadCSV('reward_statement', headers, rows);
  }

  downloadPDF(): void {
    const subtitle = `Account: ${this.authService.getAccountId() ?? ''} | Net Points: ${this.totalPoints} pts`;
    const headers = ['Date & Time', 'Transaction ID', 'Amount (INR)', 'Points'];
    const rows = this.filteredEntries.map(e => [
      this.formatDate(e.awardedOn),
      e.transactionId ?? 'REDEMPTION',
      `₹${e.transactionAmount.toFixed(2)}`,
      `${e.pointsEarned > 0 ? '+' : ''}${e.pointsEarned}`
    ]);
    this.statementService.downloadPDF('Reward Statement', subtitle, headers, rows);
  }

  // ─── Helpers ──────────────────────────────────────────────────

  refresh(): void {
    this.loadRewards();
  }

  get rewardEntries(): RewardEntry[] {
    return this.rewardData?.rewards ?? [];
  }

  get totalPoints(): number {
    return this.rewardData?.totalPoints ?? 0;
  }

  formatDate(dateString: string): string {
    if (!dateString) return '—';
    return new Intl.DateTimeFormat('en-IN', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    }).format(new Date(dateString));
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: 'INR', maximumFractionDigits: 2
    }).format(amount);
  }

  truncateId(id: string): string {
    if (!id) return '—';
    return id.length > 16 ? id.substring(0, 16) + '…' : id;
  }
}
