import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  // 🏦 ICICI TAB LOCK KEYS
  private readonly LOCK_KEY = 'icici_owner_tab';
  private readonly HEARTBEAT_KEY = 'icici_owner_heartbeat';

  private readonly TAB_ID = Math.random().toString(36).slice(2);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private isOwner = false;
  private heartbeatTimer?: any;

  private blockSubject = new BehaviorSubject<boolean>(false);
  blocked$ = this.blockSubject.asObservable();

  private currentUserSubject =
    new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());

  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {

    if (this.isBrowser) {

      this.claimOwnership();

      // release lock when tab closes
      window.addEventListener('beforeunload', () => {

        if (this.isOwner) {
          localStorage.removeItem(this.LOCK_KEY);
          localStorage.removeItem(this.HEARTBEAT_KEY);
        }
      });

      // listen if ownership changes
      window.addEventListener('storage', (event) => {

        if (event.key === this.LOCK_KEY) {

          const owner = localStorage.getItem(this.LOCK_KEY);

          if (owner && owner !== this.TAB_ID) {
            this.blockSession();
          }
        }
      });
    }
  }

  // 🏦 ICICI OWNERSHIP SYSTEM
  claimOwnership() {

    if (!this.isBrowser) return;

    const now = Date.now();
    const TIMEOUT = 5000;

    const existingOwner = localStorage.getItem(this.LOCK_KEY);
    const heartbeat = localStorage.getItem(this.HEARTBEAT_KEY);

    // 🔥 takeover if old tab dead
    if (existingOwner && heartbeat) {

      const lastBeat = Number(heartbeat);

      if (now - lastBeat > TIMEOUT) {

        console.log('🏦 Taking ownership — previous tab inactive');

        localStorage.setItem(this.LOCK_KEY, this.TAB_ID);
        this.becomeOwner();
        return;
      }
    }

    // 🚫 owner alive → block
    if (
      existingOwner &&
      existingOwner !== this.TAB_ID &&
      heartbeat &&
      now - Number(heartbeat) < TIMEOUT
    ) {
      this.blockSession();
      return;
    }

    // ✅ become owner normally
    localStorage.setItem(this.LOCK_KEY, this.TAB_ID);
    this.becomeOwner();
  }

  private becomeOwner() {

    this.isOwner = true;
    this.blockSubject.next(false);

    this.startHeartbeat();

    console.log('🏦 OWNER TAB ACTIVE:', this.TAB_ID);
  }

  private blockSession() {

    this.isOwner = false;
    this.blockSubject.next(true);

    document.body.classList.add('session-blocked');

    console.log('🚫 Session blocked — another tab active');
  }

  private startHeartbeat() {

    this.heartbeatTimer = setInterval(() => {

      if (!this.isOwner) return;

      localStorage.setItem(
        this.HEARTBEAT_KEY,
        Date.now().toString()
      );

    }, 2000);
  }

  // ✅ LOGIN
  login(credentials: LoginRequest): Observable<LoginResponse> {

    if (!this.isOwner) {
      this.blockSession();
      throw new Error('Session already active in another tab');
    }

    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/accounts/login`,
      credentials
    ).pipe(
      tap(res => this.setSession(res, credentials))
    );
  }

  logout(): void {

    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      localStorage.removeItem(this.LOCK_KEY);
      localStorage.removeItem(this.HEARTBEAT_KEY);
    }

    this.isOwner = false;
    clearInterval(this.heartbeatTimer);

    this.currentUserSubject.next(null);
  }

  private setSession(authResult: LoginResponse, credentials: LoginRequest): void {

    const basicToken = btoa(`${credentials.username}:${credentials.password}`);

    const user: AuthUser = {
      accountId: authResult.id,
      holderName: authResult.holderName,
      username: authResult.username
    };

    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, `Basic ${basicToken}`);
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    this.currentUserSubject.next(user);
  }
// ✅ REQUIRED FOR TRANSFER PAGE
getAccountId(): string | null {

  const id = this.currentUserSubject.value?.accountId;

  if (id === null || id === undefined) {
    return null;
  }

  return String(id);
}

getCurrentUser(): AuthUser | null {
  return this.currentUserSubject.value;
}

getToken(): string | null {

  if (!this.isBrowser) return null;

  return localStorage.getItem(this.TOKEN_KEY);
}

isAuthenticated(): boolean {
  return !!this.getToken();
}

  isOwnerTab(): boolean {
    return this.isOwner;
  }

  private getUserFromStorage(): AuthUser | null {

    if (!this.isBrowser) return null;

    const userStr = localStorage.getItem(this.USER_KEY);

    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  }
}
