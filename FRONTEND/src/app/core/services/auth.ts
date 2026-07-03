import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

interface LoginRequest {
  login: string;
  senha: string;
}

interface LoginResponse {
  login: string;
  token: string;
  nome: string;
}

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly apiUrl = 'http://localhost:3480/api/auth';
  private readonly tokenKey = 'nexus_token';
  private readonly nameKey = 'nexus_user_name';

  isAuthenticated = signal<boolean>(this.hasToken());

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    this.clearSession();

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      request
    ).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.nameKey, response.nome);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout(): void {
    this.clearSession();
    this.isAuthenticated.set(false);

    window.location.href = '/login';
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUserName(): string | null {
    return localStorage.getItem(this.nameKey);
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.nameKey);
    sessionStorage.clear();
  }
}
