import { Injectable, inject } from '@angular/core';
import { Auth } from './auth';

@Injectable({ providedIn: 'root' })
export class InactivityService {
  private auth = inject(Auth);
  private readonly TIMEOUT_MS = 10 * 60 * 1000; // 10 min
  private readonly CHECK_INTERVAL_MS = 60 * 1000; // 1 min
  private readonly REFRESH_THRESHOLD_MS = 2 * 60 * 1000; // renova se faltar < 2min

  private lastActivity = Date.now();
  private intervalId?: ReturnType<typeof setInterval>;

  start(): void {
    ['mousemove', 'keydown', 'click', 'scroll'].forEach((evt) =>
      window.addEventListener(evt, () => this.registrarAtividade()),
    );
    this.intervalId = setInterval(() => this.verificar(), this.CHECK_INTERVAL_MS);
  }

  stop(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }

  private registrarAtividade(): void {
    this.lastActivity = Date.now();
  }

  private verificar(): void {
    if (!this.auth.isAuthenticated()) return;

    const parado = Date.now() - this.lastActivity;
    if (parado >= this.TIMEOUT_MS) {
      this.auth.logout();
      return;
    }

    const expiraEm = this.auth.getTokenExpiration();
    if (expiraEm && expiraEm - Date.now() < this.REFRESH_THRESHOLD_MS) {
      this.auth.refresh().subscribe();
    }
  }
}
