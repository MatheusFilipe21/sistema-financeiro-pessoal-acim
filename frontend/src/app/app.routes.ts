import { Routes } from '@angular/router';
import { autenticacao as autenticacaoGuard } from './guards/autenticacao';

/**
 * Define as rotas principais da aplicação.
 *
 * @author Matheus F. N. Pereira
 */
export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./components/pages/login/login').then((m) => m.Login),
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./components/pages/cadastro/cadastro').then((m) => m.Cadastro),
  },
  {
    path: 'recuperar-senha',
    loadComponent: () =>
      import('./components/pages/recuperar-senha/recuperar-senha').then((m) => m.RecuperarSenha),
  },
  {
    path: 'redefinir-senha',
    loadComponent: () =>
      import('./components/pages/redefinir-senha/redefinir-senha').then((m) => m.RedefinirSenha),
  },
  {
    path: 'dashboard',
    canActivate: [autenticacaoGuard],
    loadComponent: () => import('./components/pages/dashboard/dashboard').then((m) => m.Dashboard),
    children: [
      {
        path: 'pessoas',
        loadComponent: () => import('./components/pages/pessoas/pessoas').then((m) => m.Pessoas),
      },
      {
        path: 'contas',
        loadComponent: () => import('./components/pages/contas/contas').then((m) => m.Contas),
      },
    ],
  },
  // Tem que ser o último.
  {
    path: '**',
    redirectTo: 'login',
  },
];
