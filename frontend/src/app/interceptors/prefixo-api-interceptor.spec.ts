import { describe, beforeEach, afterEach, it, vi, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';

import { prefixoApiInterceptor } from './prefixo-api-interceptor';
import { Token as TokenService } from '../services/token';

/**
 * Testes unitários para o prefixoApiInterceptor.
 *
 * Valida tanto a adição do prefixo "/api" quanto a injeção do Token JWT
 * utilizando um Mock do TokenService.
 *
 * @author Matheus F. N. Pereira
 */
describe('PrefixoApiInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let tokenService: TokenService;

  const TOKEN_TESTE = 'token-jwt-simulado-123';

  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([prefixoApiInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    tokenService = TestBed.inject(TokenService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Cenário: Usuário Logado + Rota Relativa.
   * Deve adicionar "/api" E o cabeçalho Authorization.
   */
  it('deve adicionar prefixo /api e cabeçalho Authorization quando houver token', () => {
    vi.spyOn(tokenService, 'obter').mockReturnValue(TOKEN_TESTE);

    httpClient.get('/pessoas').subscribe();

    const req = httpMock.expectOne('/api/pessoas');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${TOKEN_TESTE}`);
  });

  /**
   * Cenário: Usuário Deslogado + Rota Relativa (Ex: Login).
   * Deve adicionar "/api" mas NÃO enviar cabeçalho Authorization.
   */
  it('deve adicionar prefixo /api mas NÃO enviar cabeçalho se não houver token', () => {
    vi.spyOn(tokenService, 'obter').mockReturnValue(null);

    httpClient.post('/autenticacao/login', {}).subscribe();

    const req = httpMock.expectOne('/api/autenticacao/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  /**
   * Cenário: Usuário Logado + URL Absoluta (Ex: API Externa).
   * Deve manter a URL original mas adicionar o Token (comportamento padrão).
   */
  it('deve ignorar prefixo em URLs absolutas mas adicionar token se existir', () => {
    vi.spyOn(tokenService, 'obter').mockReturnValue(TOKEN_TESTE);

    httpClient.get('https://api.externa.com/dados').subscribe();

    const req = httpMock.expectOne('https://api.externa.com/dados');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${TOKEN_TESTE}`);
  });
});
