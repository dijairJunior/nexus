import { Injectable, signal, inject, DestroyRef } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from './auth';


const TIMEOUT_MS = 10 * 60 * 1000; // 10 minutos
const WARNING_MS = 30 * 1000; // aviso 30s antes

/*
const TIMEOUT_MS = 60 * 1000;      // 1 minuto (era 10 * 60 * 1000)
const WARNING_MS = 15 * 1000;      // aviso 15s antes (era 30 * 1000)
*/

@Injectable({ providedIn: 'root' })
export class InactivityService {
  private router = inject(Router);
  private auth = inject(Auth);
  private destroyRef = inject(DestroyRef);

  showWarning = signal(false);

  private inactivityTimer?: ReturnType<typeof setTimeout>;
  private warningTimer?: ReturnType<typeof setTimeout>;
  private readonly events = ['mousemove', 'keydown'] as const;
  private boundReset = () => this.resetTimers();

  start(): void {
    this.events.forEach((evt) => window.addEventListener(evt, this.boundReset, { passive: true }));
    this.resetTimers();

    this.destroyRef.onDestroy(() => this.stop());
  }

  stop(): void {
    this.events.forEach((evt) => window.removeEventListener(evt, this.boundReset));
    clearTimeout(this.inactivityTimer);
    clearTimeout(this.warningTimer);
    this.showWarning.set(false);
  }

  continueSession(): void {
    this.showWarning.set(false);

    this.auth.refresh().subscribe({
      next: () => this.resetTimers(),
      error: () => this.logout(),
    });
  }

  private resetTimers(): void {
    if (this.showWarning()) return; // não reseta silenciosamente com modal aberto
    clearTimeout(this.inactivityTimer);
    clearTimeout(this.warningTimer);

    this.warningTimer = setTimeout(() => {
      this.showWarning.set(true);
    }, TIMEOUT_MS - WARNING_MS);

    this.inactivityTimer = setTimeout(() => {
      this.logout();
    }, TIMEOUT_MS);
  }

  private logout(): void {
    this.stop();
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
