import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login').then(m => m.Login)
  },
  {
    path: 'lotes',
    canActivate: [authGuard],
    loadComponent: () => import('./features/lote/pages/lotes-list/lotes-list').then(m => m.LotesList)
  }
];
