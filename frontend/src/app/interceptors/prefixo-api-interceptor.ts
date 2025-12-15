import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Token as TokenService } from '../services/token';

/**
 * Interceptador funcional:
 * 1. Adiciona o prefixo "/api" para rotas relativas.
 * 2. Injeta o Token JWT no cabeçalho Authorization se existir no LocalStorage.
 *
 * @author Matheus F. N. Pereira
 */
export const prefixoApiInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenService = inject(TokenService);

  let urlFinal = request.url;

  if (request.url.startsWith('/')) {
    urlFinal = `/api${request.url}`;
  }

  const token = tokenService.obter();

  const requisicao = request.clone({
    url: urlFinal,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : undefined,
  });

  return next(requisicao);
};
