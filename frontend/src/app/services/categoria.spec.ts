import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Categoria as CategoriaService } from './categoria';
import { CategoriaDTO } from '../dtos/categoria/CategoriaDTO';
import { TipoCategoria } from '../enums/TipoCategoria';
import { CriarAtualizarCategoriaDTO } from '../dtos/categoria/CriarAtualizarCategoriaDTO';

/**
 * Testes unitários para o serviço {@link CategoriaService}.
 * Verifica a comunicação com os endpoints CRUD de /categorias.
 *
 * @author Matheus F. N. Pereira
 */
describe('CategoriaService', () => {
  let service: CategoriaService;
  let httpMock: HttpTestingController;

  const API_URL = '/categorias';

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   * Utiliza a nova API de testes HTTP do Angular (provideHttpClientTesting).
   */
  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), CategoriaService],
    });

    service = TestBed.inject(CategoriaService);
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
   * Deve realizar um GET em /categorias e retornar a lista de DTOs.
   */
  it('deve listar categorias (GET /categorias)', () => {
    const mockLista: CategoriaDTO[] = [
      {
        id: '1',
        nome: 'Alimentação',
        tipo: TipoCategoria.DESPESA,
        icone: 'restaurant',
        cor: '#FF0000',
        sistema: true,
      },
      {
        id: '2',
        nome: 'Salário',
        tipo: TipoCategoria.RECEITA,
        icone: 'attach_money',
        cor: '#00FF00',
        sistema: false,
      },
    ];

    service.listar().subscribe((res: string | any[]) => {
      expect(res.length).toBe(2);
      expect(res).toEqual(mockLista);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('GET');

    req.flush(mockLista);
  });

  /**
   * Testa o método cadastrar().
   * Deve realizar um POST em /categorias com o corpo correto.
   */
  it('deve cadastrar uma categoria (POST /categorias)', () => {
    const dtoEnvio: CriarAtualizarCategoriaDTO = {
      nome: 'Nova Categoria',
      tipo: TipoCategoria.DESPESA,
      icone: 'star',
      cor: '#0000FF',
    };
    const mockResposta: CategoriaDTO = {
      id: '123-uuid',
      ...dtoEnvio,
      sistema: false,
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
   * Deve realizar um PUT em /categorias/{id}.
   */
  it('deve atualizar uma categoria (PUT /categorias/{id})', () => {
    const id = '123-uuid';
    const dtoEnvio: CriarAtualizarCategoriaDTO = {
      nome: 'Categoria Editada',
      tipo: TipoCategoria.RECEITA,
      icone: 'edit',
      cor: '#FFFFFF',
    };
    const mockResposta: CategoriaDTO = {
      id: id,
      ...dtoEnvio,
      sistema: false,
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
   * Deve realizar um DELETE em /categorias/{id} e não retornar conteúdo.
   */
  it('deve excluir uma categoria (DELETE /categorias/{id})', () => {
    const id = '123-uuid';

    service.excluir(id).subscribe((res) => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(`${API_URL}/${id}`);
    expect(req.request.method).toBe('DELETE');

    req.flush(null);
  });
});
