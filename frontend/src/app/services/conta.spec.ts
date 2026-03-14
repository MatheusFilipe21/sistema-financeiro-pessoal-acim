import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { Conta } from './conta';
import { ContaDTO } from '../dtos/conta/ContaDTO';
import { CriarAtualizarContaDTO } from '../dtos/conta/CriarAtualizarContaDTO';
import { InstituicaoFinanceira } from '../enums/InstituicaoFinanceira';
import { PessoaDTO } from '../dtos/pessoa/PessoaDTO';

/**
 * Testes unitários para o serviço {@link Conta}.
 * Verifica a comunicação com os endpoints CRUD de /contas.
 *
 * @author Matheus F. N. Pereira
 */
describe('Conta', () => {
  let service: Conta;
  let httpMock: HttpTestingController;

  const API_URL = '/contas';

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   * Utiliza a nova API de testes HTTP do Angular (provideHttpClientTesting).
   */
  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), Conta],
    });

    service = TestBed.inject(Conta);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Limpa requisições pendentes após cada teste para garantir isolamento.
   */
  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Testa se o serviço é instanciado corretamente.
   */
  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  /**
   * Testa o método listar().
   * Deve realizar um GET em /contas e retornar a lista de DTOs.
   */
  it('deve listar contas (GET /contas)', () => {
    const mockPessoa: PessoaDTO = { id: 'p1', nome: 'Matheus', titular: true };

    const mockLista: ContaDTO[] = [
      {
        id: '1',
        nome: 'Reserva de Emergência',
        instituicao: InstituicaoFinanceira.MERCADO_PAGO,
        saldoInicial: 1000,
        saldoAtual: 1000,
        pessoa: mockPessoa,
      },
      {
        id: '2',
        nome: 'Conta Corrente',
        instituicao: InstituicaoFinanceira.BB,
        saldoInicial: 50,
        saldoAtual: 150,
        pessoa: mockPessoa,
      },
    ];

    service.listar().subscribe((res) => {
      expect(res.length).toBe(2);
      expect(res).toEqual(mockLista);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('GET');

    req.flush(mockLista);
  });

  /**
   * Testa o método cadastrar().
   * Deve realizar um POST em /contas com o corpo correto.
   */
  it('deve cadastrar uma conta (POST /contas)', () => {
    const dtoEnvio: CriarAtualizarContaDTO = {
      nome: 'Nova Conta',
      instituicao: InstituicaoFinanceira.ITAU,
      saldoInicial: 500,
      pessoaId: 'p1',
    };

    const mockResposta: ContaDTO = {
      id: '123-uuid',
      nome: 'Nova Conta',
      instituicao: InstituicaoFinanceira.ITAU,
      saldoInicial: 500,
      saldoAtual: 500,
      pessoa: { id: 'p1', nome: 'Matheus', titular: true },
    };

    service.cadastrar(dtoEnvio).subscribe((res) => {
      expect(res).toEqual(mockResposta);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dtoEnvio);

    req.flush(mockResposta);
  });

  /**
   * Testa o método atualizar().
   * Deve realizar um PUT em /contas/{id}.
   */
  it('deve atualizar uma conta (PUT /contas/{id})', () => {
    const id = '123-uuid';

    const dtoEnvio: CriarAtualizarContaDTO = {
      nome: 'Conta Atualizada',
      instituicao: InstituicaoFinanceira.BRADESCO,
      saldoInicial: 500,
      pessoaId: 'p1',
    };

    const mockResposta: ContaDTO = {
      id: id,
      nome: 'Conta Atualizada',
      instituicao: InstituicaoFinanceira.BRADESCO,
      saldoInicial: 500,
      saldoAtual: 500,
      pessoa: { id: 'p1', nome: 'Matheus', titular: true },
    };

    service.atualizar(id, dtoEnvio).subscribe((res) => {
      expect(res).toEqual(mockResposta);
    });

    const req = httpMock.expectOne(`${API_URL}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dtoEnvio);

    req.flush(mockResposta);
  });

  /**
   * Testa o método excluir().
   * Deve realizar um DELETE em /contas/{id} e não retornar conteúdo.
   */
  it('deve excluir uma conta (DELETE /contas/{id})', () => {
    const id = '123-uuid';

    service.excluir(id).subscribe((res) => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(`${API_URL}/${id}`);
    expect(req.request.method).toBe('DELETE');

    req.flush(null);
  });
});
