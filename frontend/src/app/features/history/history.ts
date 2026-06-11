import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../core/services/auth';
import { AccountService } from '../../core/services/account';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransactionLogResponse, TransactionStatus } from '../../core/models/transaction.model';

interface TransactionDisplay extends TransactionLogResponse {
  type: 'DEBIT' | 'CREDIT';
  displayAmount: number;
}

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    Navbar
  ],
  templateUrl: './history.html',
  styleUrl: './history.scss'
})
export class History implements OnInit {
  transactions: TransactionDisplay[] = [];
  displayedColumns: string[] = ['date', 'type', 'account', 'amount', 'status'];
  loading = true;
  errorMessage = '';
  currentAccountId: string | null = null;

  constructor(
    private authService: AuthService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.currentAccountId = this.authService.getAccountId();
    
    if (!this.currentAccountId) {
      this.errorMessage = 'Unable to load account information';
      this.loading = false;
      return;
    }

    this.loading = true;
    this.accountService.getTransactions(this.currentAccountId).subscribe({
      next: (data) => {
        this.transactions = this.processTransactions(data);
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load transaction history';
        this.loading = false;
        console.error('Error loading transactions:', error);
      }
    });
  }

  processTransactions(transactions: TransactionLogResponse[]): TransactionDisplay[] {
    return transactions.map(txn => {
      const isDebit = txn.fromAccountId === this.currentAccountId;
      const type: 'DEBIT' | 'CREDIT' = isDebit ? 'DEBIT' : 'CREDIT';
      return {
        ...txn,
        type: type,
        displayAmount: txn.amount
      };
    }).sort((a, b) => new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime());
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getOtherAccountId(transaction: TransactionDisplay): string {
    return transaction.type === 'DEBIT'
      ? transaction.toAccountId
      : transaction.fromAccountId;
  }

  refresh(): void {
    this.loadTransactions();
  }

  // Add these methods to the History class

  getDebitCount(): number {
    return this.transactions.filter(t => t.type === 'DEBIT').length;
  }

  getCreditCount(): number {
    return this.transactions.filter(t => t.type === 'CREDIT').length;
  }
}