import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    component: MainLayout,
    children: [
      {
        path: 'lotes',
        loadComponent: () =>
          import('./features/lote/pages/lotes-list/lotes-list').then((m) => m.LotesList),
      },
      {
        // [DETALHE] início — rota do Detalhe do Lote (Sprint 3)
        path: 'lotes/:id',
        loadComponent: () =>
          import('./features/lote/pages/lote-detalhe/lote-detalhe').then((m) => m.LoteDetalhe),
        children: [
          { path: '', redirectTo: 'informacoes', pathMatch: 'full' },
          {
            path: 'informacoes',
            loadComponent: () =>
              import('./features/lote/tabs/informacoes/informacoes').then((m) => m.Informacoes),
          },
          {
            path: 'produtos',
            loadComponent: () =>
              import('./features/lote/tabs/produtos/produtos').then((m) => m.Produtos),
          },
          {
            path: 'conferencia',
            loadComponent: () =>
              import('./features/lote/tabs/conferencia/conferencia').then((m) => m.Conferencia),
          },
          {
            path: 'contraprova',
            loadComponent: () =>
              import('./features/lote/tabs/contraprova/contraprova').then((m) => m.Contraprova),
          },
          {
            path: 'aprovacao',
            loadComponent: () =>
              import('./features/lote/tabs/aprovacao/aprovacao').then((m) => m.Aprovacao),
          },
          {
            path: 'relatorios',
            loadComponent: () =>
              import('./features/lote/tabs/relatorios/relatorios').then((m) => m.Relatorios),
          },
          {
            path: 'historico',
            loadComponent: () =>
              import('./features/lote/tabs/historico/historico').then((m) => m.Historico),
          },
        ],
        // [DETALHE] fim
      },
    ],
  },
];
