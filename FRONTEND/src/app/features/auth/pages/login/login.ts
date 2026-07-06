import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../../../core/services/auth'; //ja alterado!

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  login = '';
  senha = '';
  erro = signal<string | null>(null);
  carregando = signal(false);

  constructor(
    private authService: Auth,
    private router: Router,
  ) {}

  onSubmit(): void {
    this.erro.set(null);
    this.carregando.set(true);

    this.authService.login(this.login, this.senha).subscribe({ //ja alterado
      next: () => {
        this.carregando.set(false);
        this.router.navigate(['/lotes']);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Login ou senha inválidos.');
      },
    });
  }
}
