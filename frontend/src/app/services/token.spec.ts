import { TestBed } from '@angular/core/testing';
import { Token } from './token';

/**
 * Testes unitários para o serviço {@link Token}.
 *
 * <p>
 * Verifica a manipulação do LocalStorage para salvar, recuperar e remover
 * o token JWT, além da verificação de existência.
 *
 * @author Matheus F. N. Pereira
 */
describe('Token', () => {
  let service: Token;
  const CHAVE_TOKEN = 'sfp-acim-token-jwt';
  const TOKEN_TESTE = 'eyJhbGciOiJIUzUxMiJ9.exemplo-token-jwt';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Token],
    });
    service = TestBed.inject(Token);

    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  /**
   * Testa se o serviço é instanciado corretamente.
   */
  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  /**
   * Testa o método salvar().
   * Deve chamar localStorage.setItem com a chave e o valor corretos.
   */
  it('deve salvar o token no localStorage', () => {
    spyOn(localStorage, 'setItem');

    service.salvar(TOKEN_TESTE);

    expect(localStorage.setItem).toHaveBeenCalledWith(CHAVE_TOKEN, TOKEN_TESTE);
  });

  /**
   * Testa o método obter() quando existe um token.
   * Deve retornar a string do token.
   */
  it('deve recuperar o token do localStorage quando existir', () => {
    spyOn(localStorage, 'getItem').and.returnValue(TOKEN_TESTE);

    const resultado = service.obter();

    expect(localStorage.getItem).toHaveBeenCalledWith(CHAVE_TOKEN);
    expect(resultado).toBe(TOKEN_TESTE);
  });

  /**
   * Testa o método obter() quando NÃO existe token.
   * Deve retornar null.
   */
  it('deve retornar null se não houver token salvo', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);

    const resultado = service.obter();

    expect(resultado).toBeNull();
  });

  /**
   * Testa o método remover().
   * Deve chamar localStorage.removeItem com a chave correta.
   */
  it('deve remover o token do localStorage', () => {
    spyOn(localStorage, 'removeItem');

    service.remover();

    expect(localStorage.removeItem).toHaveBeenCalledWith(CHAVE_TOKEN);
  });

  /**
   * Testa o método possuiToken() quando há token.
   * Deve retornar true.
   */
  it('deve retornar true se possui token', () => {
    spyOn(service, 'obter').and.returnValue(TOKEN_TESTE);

    expect(service.possuiToken()).toBeTrue();
  });

  /**
   * Testa o método possuiToken() quando não há token.
   * Deve retornar false.
   */
  it('deve retornar false se não possui token', () => {
    spyOn(service, 'obter').and.returnValue(null);

    expect(service.possuiToken()).toBeFalse();
  });
});
