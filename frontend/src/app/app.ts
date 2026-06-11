import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <router-outlet></router-outlet>

    <div class="block-overlay" *ngIf="isBlocked">
      <div class="block-card">
        <h2>⚠️ Session Already Active</h2>
        <p>Your account is open in another tab.</p>
        <p>Please close this tab.</p>
      </div>
    </div>
  `,
  styles: [`
    .block-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.85);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 99999;
    }

    .block-card {
      background: white;
      padding: 30px;
      border-radius: 10px;
      text-align: center;
      width: 320px;
    }
  `]
})
export class App implements OnInit {

  isBlocked = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {

    this.authService.blocked$.subscribe(val => {
      this.isBlocked = val;
    });
  }
}
