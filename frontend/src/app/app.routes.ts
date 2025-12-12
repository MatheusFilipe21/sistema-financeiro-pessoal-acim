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
    loadComponent: () => import('./components/login/login').then((m) => m.Login),
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./components/cadastro/cadastro').then((m) => m.Cadastro),
  },
  {
    path: 'recuperar-senha',
    loadComponent: () =>
      import('./components/recuperar-senha/recuperar-senha').then((m) => m.RecuperarSenha),
  },
  {
    path: 'redefinir-senha',
    loadComponent: () =>
      import('./components/redefinir-senha/redefinir-senha').then((m) => m.RedefinirSenha),
  },
  {
    path: 'dashboard',
    canActivate: [autenticacaoGuard],
    loadComponent: () => import('./components/dashboard/dashboard').then((m) => m.Dashboard),
    children: [],
  },
  // Tem que ser o último.
  {
    path: '**',
    redirectTo: 'login',
  },
];
