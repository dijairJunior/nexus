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

  isAuthenticated = signal<boolean>(this.hasToken());

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  login(login: string, senha: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, { login, senha } as LoginRequest)
      .pipe(
        tap((response) => {
          localStorage.setItem(this.tokenKey, response.token);
          this.isAuthenticated.set(true);
        }),
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  refresh(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, {}).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.token);
      }),
    );
  }

  getTokenExpiration(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000; // ms
    } catch {
      return null;
    }
  }

  getUserName(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub ?? null; // só o login, já que não há claim "nome"
    } catch {
      return null;
    }
  }
}
