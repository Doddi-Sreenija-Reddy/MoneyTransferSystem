import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly THEME_KEY = 'app_theme';
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private darkModeSubject = new BehaviorSubject<boolean>(false);
  isDark$ = this.darkModeSubject.asObservable();

  constructor() {
    if (this.isBrowser) {
      const saved = localStorage.getItem(this.THEME_KEY);
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      const isDark = saved !== null ? saved === 'dark' : prefersDark;
      this.applyTheme(isDark);
    }
  }

  get isDark(): boolean {
    return this.darkModeSubject.value;
  }

  toggle(): void {
    this.applyTheme(!this.isDark);
  }

  private applyTheme(dark: boolean): void {
    this.darkModeSubject.next(dark);
    if (this.isBrowser) {
      document.body.classList.toggle('dark', dark);
      localStorage.setItem(this.THEME_KEY, dark ? 'dark' : 'light');
    }
  }
}
