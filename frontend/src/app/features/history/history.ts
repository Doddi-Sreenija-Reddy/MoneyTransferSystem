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
import { AuthService } from '../../core/services/auth';
import { AccountService } from '../../core/services/account';
import { StatementService } from '../../core/services/statement';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransactionLogResponse } from '../../core/models/transaction.model';

interface TransactionDisplay extends TransactionLogResponse {
  type: 'DEBIT' | 'CREDIT';
  displayAmount: number;
}

@Component({
  selector: 'app-history',
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

  // Date filter
  startDate = '';
  endDate = '';

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private statementService: StatementService
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
      return { ...txn, type, displayAmount: txn.amount };
    }).sort((a, b) => new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime());
  }

  // ─── Date Filter ──────────────────────────────────────────────

  get filteredTransactions(): TransactionDisplay[] {
    let txns = this.transactions;
    if (this.startDate) {
      const start = new Date(this.startDate);
      txns = txns.filter(t => new Date(t.createdOn) >= start);
    }
    if (this.endDate) {
      const end = new Date(this.endDate);
      end.setHours(23, 59, 59, 999);
      txns = txns.filter(t => new Date(t.createdOn) <= end);
    }
    return txns;
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
    const headers = ['Date & Time', 'Type', 'From Account', 'To Account', 'Amount (INR)', 'Status'];
    const rows = this.filteredTransactions.map(t => [
      this.formatDate(t.createdOn),
      t.type,
      t.fromAccountId,
      t.toAccountId,
      t.amount.toFixed(2),
      t.status
    ]);
    this.statementService.downloadCSV('transaction_statement', headers, rows);
  }

  downloadPDF(): void {
    const subtitle = `Account: ${this.currentAccountId ?? ''} | Total Transactions: ${this.filteredTransactions.length}`;
    const headers = ['Date & Time', 'Type', 'From Account', 'To Account', 'Amount (INR)', 'Status'];
    const rows = this.filteredTransactions.map(t => [
      this.formatDate(t.createdOn),
      t.type,
      t.fromAccountId,
      t.toAccountId,
      `₹${t.amount.toFixed(2)}`,
      t.status
    ]);
    this.statementService.downloadPDF('Transaction Statement', subtitle, headers, rows);
  }

  // ─── Helpers ──────────────────────────────────────────────────

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    }).format(date);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: 'INR'
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

  getDebitCount(): number {
    return this.filteredTransactions.filter(t => t.type === 'DEBIT').length;
  }

  getCreditCount(): number {
    return this.filteredTransactions.filter(t => t.type === 'CREDIT').length;
  }
}