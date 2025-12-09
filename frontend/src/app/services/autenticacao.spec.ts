import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { Autenticacao } from './autenticacao';
import { DadosCadastroUsuarioDTO } from '../dtos/usuario/DadosCadastroUsuarioDTO';
import { UsuarioDTO } from '../dtos/usuario/UsuarioDTO';
import { DadosAutenticacaoDTO } from '../dtos/autenticacao/DadosAutenticacaoDTO';
import { DadosTokenJWTDTO } from '../dtos/autenticacao/DadosTokenJWTDTO';

/**
 * Testes unitários para o serviço {@link Autenticacao}.
 *
 * @author Matheus F. N. Pereira
 */
describe('Autenticacao', () => {
  let service: Autenticacao;
  let httpMock: HttpTestingController;

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   */
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), Autenticacao],
    });

    service = TestBed.inject(Autenticacao);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Verifica se não ficaram requisições pendentes após cada teste.
   */
  afterEach(() => {
    httpMock.verify();
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
  it('deve registrar um usuário (POST /autenticacao/cadastro)', (done) => {
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
      done();
    });

    const req = httpMock.expectOne('/autenticacao/cadastro');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dadosCadastro);

    req.flush(mockResponse);
  });

  /**
   * Testa o método login(), garantindo que o endpoint correto (POST /autenticacao/login) seja chamado,
   * que o DTO enviado esteja correto e que o retorno do backend (DadosTokenJWTDTO) seja recebido.
   */
  it('deve autenticar um usuário (POST /autenticacao/login)', (done) => {
    const dadosLogin: DadosAutenticacaoDTO = {
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
    };
    const mockResponse: DadosTokenJWTDTO = {
      token:
        'eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2MzMwNjE3NiwiZXhwIjoxNzYzMzM0OTc2fQ.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ',
    };

    service.login(dadosLogin).subscribe((res) => {
      expect(res).toEqual(mockResponse);
      done();
    });

    const req = httpMock.expectOne('/autenticacao/login');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dadosLogin);

    req.flush(mockResponse);
  });

  /**
   * Testa o método esqueciSenha(), garantindo que o endpoint correto (POST /autenticacao/esqueci-senha) seja chamado,
   * que o e-mail seja enviado no corpo da requisição e que o retorno seja vazio (void).
   */
  it('deve solicitar recuperação de senha (POST /autenticacao/esqueci-senha)', (done) => {
    const email = 'matheusfnpereira@gmail.com';

    service.esqueciSenha(email).subscribe(() => {
      done();
    });

    const req = httpMock.expectOne('/autenticacao/esqueci-senha');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: email });

    req.flush(null);
  });
});
