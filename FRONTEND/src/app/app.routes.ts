import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login').then((m) => m.Login),
  },

  {
    path: '',
    canActivate: [authGuard],

    loadComponent: () => import('./layout/main-layout/main-layout').then((m) => m.MainLayout),
    children: [
      {
        path: 'lotes',
        loadComponent: () =>
          import('./features/lote/pages/lotes-list/lotes-list').then(m => m.LotesList),
      },
    ],
  },
];
