import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { AuthService } from '../../core/services/auth';
import { AccountService } from '../../core/services/account';
import { TransferService } from '../../core/services/transfer';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransferRequest } from '../../core/models/transfer.model';
import { AccountResponse } from '../../core/models/account.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    Navbar
  ],
  templateUrl: './transfer.html',
  styleUrl: './transfer.scss'
})
export class Transfer implements OnInit {

  transferForm: FormGroup;
  currentAccount: AccountResponse | null = null;
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  formSubmitted = false; // 🔥 important
  /** True when the failed transfer was recorded in transaction history (404, 403, 400) */
  failedTransferRecorded = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private accountService: AccountService,
    private transferService: TransferService,
    private router: Router
  ) {

    this.transferForm = this.fb.group({
      toAccountId: ['', [Validators.required, Validators.pattern(/^ACC-\d+$/)]],
      amount: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadCurrentAccount();
  }

  loadCurrentAccount(): void {

    const accountId = this.authService.getAccountId();

    if (!accountId) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;

    this.accountService.getAccount(accountId).subscribe({
      next: (data) => {
        this.currentAccount = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading account:', error);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {

    this.formSubmitted = true; // 🔥 trigger validation
    this.clearMessages();
    this.failedTransferRecorded = false;

    // show validation instantly
    this.transferForm.markAllAsTouched();

    const amountControl = this.transferForm.get('amount');

    // 🚨 BANK STYLE ALERT
    if (!amountControl?.value) {
      this.errorMessage = '⚠️ Please enter the transfer amount';
      return;
    }

    if (this.transferForm.invalid || !this.currentAccount) {
      return;
    }

    const toAccountId: string = this.transferForm.value.toAccountId;

    if (toAccountId === this.currentAccount.id) {
      this.errorMessage = 'Cannot transfer to the same account';
      return;
    }

    this.submitting = true;

    const transferRequest: TransferRequest = {
      fromAccountId: this.currentAccount.id,
      toAccountId: toAccountId,
      amount: parseFloat(this.transferForm.value.amount),
      idempotencyKey: this.transferService.generateIdempotencyKey()
    };

    this.transferService.transfer(transferRequest).subscribe({
      next: (response) => {

        this.submitting = false;

        this.successMessage =
          `Transfer successful! ${this.formatCurrency(response.amount)} sent to Account ${response.creditedTo}`;

        this.transferForm.reset();
        this.formSubmitted = false;

        this.loadCurrentAccount();

        setTimeout(() => {
          this.router.navigate(['/history']);
        }, 2000);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage =
          error.error?.message || 'Transfer failed. Please try again.';
        // Backend records failed transactions for 404 (not found), 403 (not active), 400 (insufficient balance)
        this.failedTransferRecorded = [400, 403, 404].includes(error.status);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
