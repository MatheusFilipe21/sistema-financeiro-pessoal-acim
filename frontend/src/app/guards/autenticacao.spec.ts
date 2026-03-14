import { describe, beforeEach, it, vi, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { autenticacao as autenticacaoGuard } from './autenticacao';
import { Autenticacao as AutenticacaoService } from '../services/autenticacao';

/**
 * Mock do AutenticacaoService.
 * Criamos um Spy para o método possuiTokenValido para controlar o retorno nos testes.
 */
class AutenticacaoServiceMock {
  possuiTokenValido = vi.fn();
}

/**
 * Mock do Router para verificar o redirecionamento.
 */
class RouterMock {
  navigate = vi.fn();
}

/**
 * Testes unitários para o Guard {@link autenticacao}.
 *
 * @author Matheus F. N. Pereira
 */
describe('autenticacao Guard', () => {
  let autenticacaoService: AutenticacaoServiceMock;
  let router: RouterMock;

  const routeMock = {} as ActivatedRouteSnapshot;
  const stateMock = {} as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        { provide: AutenticacaoService, useClass: AutenticacaoServiceMock },
        { provide: Router, useClass: RouterMock },
      ],
    });

    router = TestBed.inject(Router) as any;
    autenticacaoService = TestBed.inject(AutenticacaoService) as any;
    vi.spyOn(autenticacaoService, 'possuiTokenValido').mockReturnValue(true);
  });

  /**
   * Cenário: Token válido.
   * Resultado esperado: O guard deve retornar TRUE e NÃO deve navegar para login.
   */
  it('deve permitir o acesso se o token for válido', () => {
    autenticacaoService.possuiTokenValido.mockReturnValue(true);

    const resultado = TestBed.runInInjectionContext(() => autenticacaoGuard(routeMock, stateMock));

    expect(resultado).toBe(true);
    expect(autenticacaoService.possuiTokenValido).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  /**
   * Cenário: Token inválido ou inexistente (RNF16).
   * Resultado esperado: O guard deve retornar FALSE e deve redirecionar para /login.
   */
  it('deve bloquear o acesso e redirecionar para /login se o token for inválido', () => {
    autenticacaoService.possuiTokenValido.mockReturnValue(false);

    const resultado = TestBed.runInInjectionContext(() => autenticacaoGuard(routeMock, stateMock));

    expect(resultado).toBe(false);
    expect(autenticacaoService.possuiTokenValido).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
