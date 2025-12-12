import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Autenticacao as AutenticacaoService } from '../services/autenticacao';

/**
 * Guardião de rota responsável por proteger o acesso ao dashboard.
 * Verifica a existência e validade do token JWT via AutenticacaoService (RF21).
 *
 * @author Matheus F. N. Pereira
 */
export const autenticacao: CanActivateFn = (route, state) => {
  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);

  if (autenticacaoService.possuiTokenValido()) {
    return true;
  }

  // (RNF16) Redirecionamento imediato caso o token seja inválido/inexistente
  router.navigate(['/login']);
  return false;
};
