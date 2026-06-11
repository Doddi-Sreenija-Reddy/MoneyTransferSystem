import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-login',
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
    MatProgressSpinnerModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {

    const registered = this.route.snapshot.queryParams['registered'] === 'true';
    if (registered) {
      this.successMessage = 'Account created. Please sign in.';
    }
this.loginForm = this.fb.group({
  username: [
    '',
    [
      Validators.required,
      Validators.pattern(/^[a-zA-Z0-9]{5,}$/) 
      // letters + numbers only, min length 5
    ]
  ],
  password: [
    '',
    [
      Validators.required,
      Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{8,}$/)

      // min 8 chars, 1 uppercase, 1 number, 1 special char
    ]
  ]
});

  }

  onSubmit(): void {

    // 🔥 HARD BLOCK SECOND TAB BEFORE LOGIN CALL
    if (!this.authService.isOwnerTab()) {
      this.errorMessage = 'Session already active in another tab.';
      return;
    }

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigate([returnUrl]);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Invalid username or password';
      }
    });
  }

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }
}
