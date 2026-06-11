import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth';
import { RewardService } from '../../core/services/reward';
import { Navbar } from '../../shared/components/navbar/navbar';
import { RewardEntry, RewardResponse } from '../../core/models/reward.model';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
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

  constructor(
    private authService: AuthService,
    private rewardService: RewardService
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

  formatDate(dateString: string): string {
    return new Intl.DateTimeFormat('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(dateString));
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount);
  }

  truncateId(id: string): string {
    return id.length > 16 ? id.substring(0, 16) + '…' : id;
  }

  refresh(): void {
    this.loadRewards();
  }

  get rewardEntries(): RewardEntry[] {
    return this.rewardData?.rewards ?? [];
  }

  get totalPoints(): number {
    return this.rewardData?.totalPoints ?? 0;
  }
}
