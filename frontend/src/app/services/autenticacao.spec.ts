import { describe, beforeEach, afterEach, it, vi, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

import { Autenticacao } from './autenticacao';
import { DadosCadastroUsuarioDTO } from '../dtos/usuario/DadosCadastroUsuarioDTO';
import { UsuarioDTO } from '../dtos/usuario/UsuarioDTO';
import { DadosAutenticacaoDTO } from '../dtos/autenticacao/DadosAutenticacaoDTO';
import { DadosTokenJWTDTO } from '../dtos/autenticacao/DadosTokenJWTDTO';
import { DadosRedefinicaoSenhaDTO } from '../dtos/autenticacao/DadosRedefinicaoSenhaDTO';
import { Dashboard } from '../components/pages/dashboard/dashboard';

/**
 * Testes unitários para o serviço {@link Autenticacao}.
 *
 * @author Matheus F. N. Pereira
 */
describe('Autenticacao', () => {
  let service: Autenticacao;
  let httpMock: HttpTestingController;
  let router: Router;

  const TOKEN_VALIDO =
    'eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjk5OTk5OTk5OTl9.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ';
  const TOKEN_EXPIRADO =
    'eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjF9.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ';

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   */
  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([{ path: 'dashboard', component: Dashboard }])],
      providers: [provideHttpClient(), provideHttpClientTesting(), Autenticacao],
    });

    vi.spyOn(Storage.prototype, 'setItem');
    vi.spyOn(Storage.prototype, 'getItem');
    vi.spyOn(Storage.prototype, 'removeItem');
    vi.spyOn(console, 'warn').mockImplementation(() => {});

    service = TestBed.inject(Autenticacao);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  /**
   * Limpa requisições pendentes e o LocalStorage após cada teste.
   */
  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  /**
   * Testa se o serviço é criado com sucesso.
   */
  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  /**
   * Testa o método registrar(), garantindo que o endpoint correto (POST /autenticacao/cadastro) seja chamado,
   * que o DTO enviado esteja correto e que o retorno do backend (UsuarioDTO) seja recebido conforme esperado.
   */
  it('deve registrar um usuário (POST /autenticacao/cadastro)', () => {
    const dadosCadastro: DadosCadastroUsuarioDTO = {
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
    };

    const mockResponse: UsuarioDTO = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
    };

    service.registrar(dadosCadastro).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/autenticacao/cadastro');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dadosCadastro);

    req.flush(mockResponse);
  });

  /**
   * Testa o método login(), garantindo que além de chamar a API,
   * ele invoca a lógica de persistência de sessão (logar).
   */
  it('deve autenticar um usuário e salvar a sessão (POST /autenticacao/login)', () => {
    const dadosLogin: DadosAutenticacaoDTO = {
      email: 'matheus@gmail.com',
      senha: 'Ab123456',
    };
    const mockResponse: DadosTokenJWTDTO = {
      token: TOKEN_VALIDO,
    };

    vi.spyOn(service, 'logar');

    service.login(dadosLogin).subscribe((res) => {
      expect(res).toEqual(mockResponse);
      expect(service.logar).toHaveBeenCalledWith(TOKEN_VALIDO);
    });

    const req = httpMock.expectOne('/autenticacao/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  /**
   * Testa o método recuperarSenha(), garantindo que o endpoint correto (POST /autenticacao/recuperar-senha) seja chamado,
   * que o e-mail seja enviado no corpo da requisição e que o retorno seja vazio (void).
   */
  it('deve solicitar recuperação de senha (POST /autenticacao/recuperar-senha)', () => {
    const email = 'matheusfnpereira@gmail.com';

    service.recuperarSenha(email).subscribe(() => {});

    const req = httpMock.expectOne('/autenticacao/recuperar-senha');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: email });

    req.flush(null);
  });

  /**
   * Testa o método redefinirSenha(), garantindo que o endpoint correto (POST /autenticacao/redefinir-senha) seja chamado,
   * que o token e a nova senha sejam enviados no corpo da requisição e que o retorno seja vazio (void).
   */
  it('deve redefinir a senha', () => {
    const dados: DadosRedefinicaoSenhaDTO = {
      token:
        'eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2NTMyMjMzOSwiZXhwIjoxNzY1MzM2NzM5fQ.sfznaHFA4WvRXHHflYWzrHw1-v3nqS3Oz80DaUOFP4g6RpVbbNOrSwOWcEWfc19cJkygSTSPYAnlzXeFU4vRxw',
      senha: 'NovaSenha123!',
    };

    service.redefinirSenha(dados).subscribe((res) => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(`/autenticacao/redefinir-senha`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dados);
    req.flush(null);
  });

  /**
   * Testa o método logar().
   * Deve verificar se o token é salvo no localStorage, se o Signal é atualizado
   * e se ocorre o redirecionamento para o dashboard.
   */
  it('deve salvar sessão, atualizar signal e navegar para dashboard', () => {
    vi.spyOn(localStorage, 'setItem');
    vi.spyOn(router, 'navigate');

    service.logar(TOKEN_VALIDO);

    expect(localStorage.setItem).toHaveBeenCalledWith('sfp-acim-token-jwt', TOKEN_VALIDO);
    expect(service.usuarioEstaLogado()).toBe(true);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  /**
   * Testa o método deslogar().
   * Deve verificar se o token é removido, o Signal atualizado para false
   * e se ocorre o redirecionamento para o login.
   */
  it('deve limpar sessão, atualizar signal e navegar para login', () => {
    vi.spyOn(localStorage, 'removeItem');
    vi.spyOn(router, 'navigate');

    service.deslogar();

    expect(localStorage.removeItem).toHaveBeenCalledWith('sfp-acim-token-jwt');
    expect(service.usuarioEstaLogado()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  /**
   * Testa a validação de token (possuiTokenValido) com um JWT válido (data futura).
   */
  it('deve retornar true para token válido (não expirado)', () => {
    vi.spyOn(localStorage, 'getItem').mockReturnValue(TOKEN_VALIDO);
    expect(service.possuiTokenValido()).toBe(true);
  });

  /**
   * Testa a validação de token com um JWT expirado (data passada).
   */
  it('deve retornar false para token expirado', () => {
    vi.spyOn(localStorage, 'getItem').mockReturnValue(TOKEN_EXPIRADO);
    expect(service.possuiTokenValido()).toBe(false);
  });

  /**
   * Testa a validação quando não há token no armazenamento.
   */
  it('deve retornar false se não houver token', () => {
    vi.spyOn(localStorage, 'getItem').mockReturnValue(null);
    expect(service.possuiTokenValido()).toBe(false);
  });

  /**
   * Testa a robustez contra tokens malformados (não JWTs).
   * Garante que o serviço não quebra e retorna false.
   */
  it('deve retornar false e logar aviso se o token for malformado', () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    vi.spyOn(localStorage, 'getItem').mockReturnValue('token-invalido-que-nao-eh-jwt');

    expect(service.possuiTokenValido()).toBe(false);
    expect(warnSpy).toHaveBeenCalled();
  });
});
