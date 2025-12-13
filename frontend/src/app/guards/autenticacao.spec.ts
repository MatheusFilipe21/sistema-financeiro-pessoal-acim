import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { autenticacao as autenticacaoGuard } from './autenticacao';
import { Autenticacao as AutenticacaoService } from '../services/autenticacao';

/**
 * Mock do AutenticacaoService.
 * Criamos um Spy para o método possuiTokenValido para controlar o retorno nos testes.
 */
class AutenticacaoServiceMock {
  possuiTokenValido = jasmine.createSpy('possuiTokenValido');
}

/**
 * Mock do Router para verificar o redirecionamento.
 */
class RouterMock {
  navigate = jasmine.createSpy('navigate');
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
    TestBed.configureTestingModule({
      providers: [
        { provide: AutenticacaoService, useClass: AutenticacaoServiceMock },
        { provide: Router, useClass: RouterMock },
      ],
    });

    autenticacaoService = TestBed.inject(AutenticacaoService) as any;
    router = TestBed.inject(Router) as any;
  });

  /**
   * Cenário: Token válido.
   * Resultado esperado: O guard deve retornar TRUE e NÃO deve navegar para login.
   */
  it('deve permitir o acesso se o token for válido', () => {
    autenticacaoService.possuiTokenValido.and.returnValue(true);

    const resultado = TestBed.runInInjectionContext(() => autenticacaoGuard(routeMock, stateMock));

    expect(resultado).toBeTrue();
    expect(autenticacaoService.possuiTokenValido).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  /**
   * Cenário: Token inválido ou inexistente (RNF16).
   * Resultado esperado: O guard deve retornar FALSE e deve redirecionar para /login.
   */
  it('deve bloquear o acesso e redirecionar para /login se o token for inválido', () => {
    autenticacaoService.possuiTokenValido.and.returnValue(false);

    const resultado = TestBed.runInInjectionContext(() => autenticacaoGuard(routeMock, stateMock));

    expect(resultado).toBeFalse();
    expect(autenticacaoService.possuiTokenValido).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
