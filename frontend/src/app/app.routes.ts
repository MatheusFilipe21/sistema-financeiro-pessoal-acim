import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Cadastro } from './components/cadastro/cadastro';
import { RecuperarSenha } from './components/recuperar-senha/recuperar-senha';
import { RedefinirSenha } from './components/redefinir-senha/redefinir-senha';

/**
 * Define as rotas principais da aplicação.
 *
 * @author Matheus F. N. Pereira
 */
export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'cadastro', component: Cadastro },
  { path: 'recuperar-senha', component: RecuperarSenha },
  { path: 'redefinir-senha', component: RedefinirSenha },
];
