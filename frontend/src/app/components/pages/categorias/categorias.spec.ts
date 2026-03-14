import { describe, beforeEach, it, vi, expect, Mocked } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Categorias } from './categorias';
import { Categoria as CategoriaService } from '../../../services/categoria';
import { Dialog as DialogService } from '../../../services/dialog';
import { CategoriaDTO } from '../../../dtos/categoria/CategoriaDTO';
import { TipoCategoria } from '../../../enums/TipoCategoria';

/**
 * Testes unitários para o componente de página {@link Categorias}.
 *
 * Cobre a lógica de listagem, filtragem avançada (Tipo/Nome) e
 * orquestração das ações de CRUD, incluindo proteção de categorias do sistema.
 *
 * @author Matheus F. N. Pereira
 */
describe('Categorias', () => {
  let component: Categorias;
  let fixture: ComponentFixture<Categorias>;
  let categoriaServiceSpy: Mocked<CategoriaService>;
  let dialogServiceSpy: Mocked<DialogService>;

  const listaCategoriasMock: CategoriaDTO[] = [
    {
      id: '1',
      nome: 'Salário',
      tipo: TipoCategoria.RECEITA,
      sistema: false,
      cor: '#fff',
      icone: 'money',
    },
    {
      id: '2',
      nome: 'Alimentação',
      tipo: TipoCategoria.DESPESA,
      sistema: false,
      cor: '#fff',
      icone: 'food',
    },
    {
      id: '3',
      nome: 'Transferência',
      tipo: TipoCategoria.AMBOS,
      sistema: true,
      cor: '#fff',
      icone: 'swap',
    },
  ];

  beforeEach(async () => {
    categoriaServiceSpy = {
      listar: vi.fn(),
    } as unknown as Mocked<CategoriaService>;

    dialogServiceSpy = {
      abrirFormularioCategoria: vi.fn(),
    } as unknown as Mocked<DialogService>;

    categoriaServiceSpy.listar.mockReturnValue(of(listaCategoriasMock));

    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [Categorias, FormsModule],
      providers: [
        { provide: CategoriaService, useValue: categoriaServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Categorias);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  /**
   * Verifica se o componente foi criado com sucesso.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Teste de Inicialização e Carregamento de Dados.
   */
  it('deve carregar dados ao iniciar (ngOnInit)', () => {
    expect(categoriaServiceSpy.listar).toHaveBeenCalled();
    expect(component.categorias).toEqual(listaCategoriasMock);
    expect(component.categoriasFiltradas).toEqual(listaCategoriasMock);
  });

  /**
   * Teste de Filtragem por Nome.
   */
  it('deve filtrar corretamente por nome', () => {
    component.filtro.nome = 'salário';
    component.filtrar();

    expect(component.categoriasFiltradas.length).toBe(1);
    expect(component.categoriasFiltradas[0].id).toBe('1');
  });

  /**
   * Teste de Filtragem por Tipo (Lógica Específica).
   * Regra: Selecionar "RECEITA" deve trazer (Receita + Ambos).
   */
  it('deve incluir categorias do tipo AMBOS ao filtrar por RECEITA', () => {
    component.filtro.tipo = TipoCategoria.RECEITA;
    component.filtrar();

    expect(component.categoriasFiltradas.length).toBe(2);
    expect(component.categoriasFiltradas.some((c) => c.tipo === TipoCategoria.RECEITA)).toBe(true);
    expect(component.categoriasFiltradas.some((c) => c.tipo === TipoCategoria.AMBOS)).toBe(true);
  });

  /**
   * Teste de Filtragem por Tipo (Lógica Específica).
   * Regra: Selecionar "DESPESA" deve trazer (Despesa + Ambos).
   */
  it('deve incluir categorias do tipo AMBOS ao filtrar por DESPESA', () => {
    component.filtro.tipo = TipoCategoria.DESPESA;
    component.filtrar();

    expect(component.categoriasFiltradas.length).toBe(2);
    expect(component.categoriasFiltradas.some((c) => c.tipo === TipoCategoria.DESPESA)).toBe(true);
    expect(component.categoriasFiltradas.some((c) => c.tipo === TipoCategoria.AMBOS)).toBe(true);
  });

  /**
   * Teste de Reset.
   */
  it('deve limpar os filtros e restaurar a lista completa', () => {
    component.filtro.nome = 'teste';
    component.filtro.tipo = TipoCategoria.RECEITA;
    component.filtrar();

    component.limpar();

    expect(component.filtro.nome).toBe('');
    expect(component.filtro.tipo).toBeNull();
    expect(component.categoriasFiltradas.length).toBe(3);
  });

  /**
   * Teste da função auxiliar de bloqueio.
   */
  it('deve retornar true para bloqueio apenas se a categoria for do sistema', () => {
    const catSistema = listaCategoriasMock[2];
    const catUsuario = listaCategoriasMock[0];

    expect(component.verificarBloqueioAcao(catSistema)).toBe(true);
    expect(component.verificarBloqueioAcao(catUsuario)).toBe(false);
  });

  /**
   * Teste do Fluxo de Adicionar.
   */
  it('deve abrir dialog de cadastro e atualizar lista ao confirmar', () => {
    const novaCategoria: CategoriaDTO = {
      ...listaCategoriasMock[0],
      id: '99',
    };
    dialogServiceSpy.abrirFormularioCategoria.mockReturnValue(of(novaCategoria));

    component.aoAdicionar();

    expect(dialogServiceSpy.abrirFormularioCategoria).toHaveBeenCalledWith('cadastrar');
    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  /**
   * Teste do Fluxo de Editar (Categoria do Usuário).
   */
  it('deve abrir dialog de edição para categoria do usuário', () => {
    const catUsuario = listaCategoriasMock[0];
    const catEditada = { ...catUsuario, nome: 'Editado' };

    dialogServiceSpy.abrirFormularioCategoria.mockReturnValue(of(catEditada));

    component.aoEditar(catUsuario);

    expect(dialogServiceSpy.abrirFormularioCategoria).toHaveBeenCalledWith('editar', catUsuario);
    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  /**
   * Teste do Bloqueio de Edição (Categoria do Sistema).
   */
  it('NÃO deve abrir dialog de edição para categoria do sistema', () => {
    const catSistema = listaCategoriasMock[2];

    component.aoEditar(catSistema);

    expect(dialogServiceSpy.abrirFormularioCategoria).not.toHaveBeenCalled();
    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(1);
  });

  /**
   * Teste do Fluxo de Excluir (Categoria do Usuário).
   */
  it('deve abrir dialog de exclusão para categoria do usuário', () => {
    const catUsuario = listaCategoriasMock[0];

    dialogServiceSpy.abrirFormularioCategoria.mockReturnValue(of(true));

    component.aoExcluir(catUsuario);

    expect(dialogServiceSpy.abrirFormularioCategoria).toHaveBeenCalledWith('excluir', catUsuario);
    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  /**
   * Teste do Bloqueio de Exclusão (Categoria do Sistema).
   */
  it('NÃO deve abrir dialog de exclusão para categoria do sistema', () => {
    const catSistema = listaCategoriasMock[2];

    component.aoExcluir(catSistema);

    expect(dialogServiceSpy.abrirFormularioCategoria).not.toHaveBeenCalled();
    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(1);
  });

  /**
   * Teste de filtros com valores vazios.
   */
  it('deve manter a lista original quando os filtros forem vazios ou nulos', () => {
    component.filtro.nome = '   ';
    component.filtro.tipo = null;

    component.filtrar();

    expect(component.categoriasFiltradas.length).toBe(3);
    expect(component.categoriasFiltradas).toEqual(listaCategoriasMock);
  });

  /**
   * Teste de cancelamento de dialogs.
   */
  it('não deve atualizar a lista se o usuário cancelar os dialogs (Adicionar/Editar/Excluir)', () => {
    dialogServiceSpy.abrirFormularioCategoria.mockReturnValue(of(undefined));

    component.aoAdicionar();
    component.aoEditar(listaCategoriasMock[0]);
    component.aoExcluir(listaCategoriasMock[0]);

    expect(categoriaServiceSpy.listar).toHaveBeenCalledTimes(1);
  });
});
