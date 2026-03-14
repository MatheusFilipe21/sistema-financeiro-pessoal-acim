import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { Pessoa } from './pessoa';
import { PessoaDTO } from '../dtos/pessoa/PessoaDTO';
import { CriarAtualizarPessoaDTO } from '../dtos/pessoa/CriarAtualizarPessoaDTO';

/**
 * Testes unitários para o serviço {@link Pessoa}.
 * Verifica a comunicação com os endpoints CRUD de /pessoas.
 *
 * @author Matheus F. N. Pereira
 */
describe('Pessoa', () => {
  let service: Pessoa;
  let httpMock: HttpTestingController;

  const API_URL = '/pessoas';

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   * Utiliza a nova API de testes HTTP do Angular (provideHttpClientTesting).
   */
  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), Pessoa],
    });

    service = TestBed.inject(Pessoa);
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
   * Testa o método listar() (RF47).
   * Deve realizar um GET em /pessoas e retornar a lista de DTOs.
   */
  it('deve listar pessoas (GET /pessoas)', () => {
    const mockLista: PessoaDTO[] = [
      { id: '1', nome: 'Matheus', titular: true },
      { id: '2', nome: 'Ana', titular: true },
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
   * Testa o método cadastrar() (RF46).
   * Deve realizar um POST em /pessoas com o corpo correto.
   */
  it('deve cadastrar uma pessoa (POST /pessoas)', () => {
    const dtoEnvio: CriarAtualizarPessoaDTO = { nome: 'Nova Pessoa', titular: true };
    const mockResposta: PessoaDTO = { id: '123-uuid', nome: 'Nova Pessoa', titular: true };

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
   * Deve realizar um PUT em /pessoas/{id}.
   */
  it('deve atualizar uma pessoa (PUT /pessoas/{id})', () => {
    const id = '123-uuid';
    const dtoEnvio: CriarAtualizarPessoaDTO = { nome: 'Nome Atualizado', titular: true };
    const mockResposta: PessoaDTO = { id: id, nome: 'Nome Atualizado', titular: true };

    service.atualizar(id, dtoEnvio).subscribe((res) => {
      expect(res).toEqual(mockResposta);
    });

    const req = httpMock.expectOne(`${API_URL}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dtoEnvio);

    req.flush(mockResposta);
  });

  /**
   * Testa o método excluir() (RF49).
   * Deve realizar um DELETE em /pessoas/{id} e não retornar conteúdo.
   */
  it('deve excluir uma pessoa (DELETE /pessoas/{id})', () => {
    const id = '123-uuid';

    service.excluir(id).subscribe((res) => {
      expect(res).toBeNull(); // Void retorna null no teste
    });

    const req = httpMock.expectOne(`${API_URL}/${id}`);
    expect(req.request.method).toBe('DELETE');

    req.flush(null);
  });
});
