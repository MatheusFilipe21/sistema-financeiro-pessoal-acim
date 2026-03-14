import { describe, beforeEach, it, expect, vi, Mocked } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Contas } from './contas';
import { Conta as ContaService } from '../../../services/conta';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { Dialog as DialogService } from '../../../services/dialog';
import { ContaDTO } from '../../../dtos/conta/ContaDTO';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import { InstituicaoFinanceira } from '../../../enums/InstituicaoFinanceira';

/**
 * Testes unitários para o componente de página {@link Contas}.
 *
 * Cobre a lógica de listagem, carregamento de opções de filtro,
 * filtragem avançada (múltipla escolha) e orquestração das ações de CRUD.
 *
 * @author Matheus F. N. Pereira
 */
describe('Contas', () => {
  let component: Contas;
  let fixture: ComponentFixture<Contas>;
  let contaServiceSpy: Mocked<ContaService>;
  let pessoaServiceSpy: Mocked<PessoaService>;
  let dialogServiceSpy: Mocked<DialogService>;

  const mockTitular1: PessoaDTO = { id: 'p1', nome: 'Ana', titular: true };
  const mockTitular2: PessoaDTO = { id: 'p2', nome: 'Bruno', titular: true };
  const mockDependente: PessoaDTO = { id: 'p3', nome: 'Carlos', titular: false };

  const listaPessoasMock: PessoaDTO[] = [mockTitular2, mockDependente, mockTitular1];

  const listaContasMock: ContaDTO[] = [
    {
      id: '1',
      nome: 'Nubank Ana',
      instituicao: 'NUBANK' as InstituicaoFinanceira,
      saldoAtual: 100,
      saldoInicial: 100,
      pessoa: mockTitular1,
    },
    {
      id: '2',
      nome: 'Itaú Bruno',
      instituicao: 'ITAU' as InstituicaoFinanceira,
      saldoAtual: 200,
      saldoInicial: 200,
      pessoa: mockTitular2,
    },
    {
      id: '3',
      nome: 'Inter Ana',
      instituicao: 'INTER' as InstituicaoFinanceira,
      saldoAtual: 50,
      saldoInicial: 50,
      pessoa: mockTitular1,
    },
  ];

  beforeEach(async () => {
    contaServiceSpy = {
      listar: vi.fn(),
    } as unknown as Mocked<ContaService>;

    pessoaServiceSpy = {
      listar: vi.fn(),
    } as unknown as Mocked<PessoaService>;

    dialogServiceSpy = {
      abrirFormularioConta: vi.fn(),
    } as unknown as Mocked<DialogService>;

    contaServiceSpy.listar.mockReturnValue(of(listaContasMock));
    pessoaServiceSpy.listar.mockReturnValue(of(listaPessoasMock));

    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [Contas, FormsModule],
      providers: [
        { provide: ContaService, useValue: contaServiceSpy },
        { provide: PessoaService, useValue: pessoaServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Contas);
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
   * Verifica se carrega as contas e se prepara as opções do filtro corretamente.
   */
  it('deve carregar contas e opções de filtro (ordenadas e filtradas) ao iniciar', () => {
    expect(contaServiceSpy.listar).toHaveBeenCalled();
    expect(component.contas.length).toBe(3);

    expect(component.opcoesPessoas.length).toBe(2);

    expect(component.opcoesPessoas[0].nome).toBe('Ana');
    expect(component.opcoesPessoas[1].nome).toBe('Bruno');
  });

  /**
   * Teste de Filtragem (Nome, Instituição e Titular).
   */
  it('deve filtrar corretamente por nome, instituição e titular', () => {
    component.filtro.nome = 'nubank';
    component.filtrar();
    expect(component.contasFiltradas.length).toBe(1);
    expect(component.contasFiltradas[0].nome).toBe('Nubank Ana');

    component.filtro.nome = '';

    component.filtro.pessoasIds = ['p1'];
    component.filtrar();
    expect(component.contasFiltradas.length).toBe(2);

    component.filtro.instituicoes = ['INTER' as InstituicaoFinanceira];
    component.filtrar();
    expect(component.contasFiltradas.length).toBe(1);
    expect(component.contasFiltradas[0].nome).toBe('Inter Ana');
  });

  /**
   * Teste de Reset.
   */
  it('deve limpar os filtros e restaurar a lista completa', () => {
    component.filtro.nome = 'filtro sujo';
    component.filtro.pessoasIds = ['p1'];
    component.filtrar();

    component.limpar();

    expect(component.filtro.nome).toBe('');
    expect(component.filtro.pessoasIds).toEqual([]);
    expect(component.filtro.instituicoes).toEqual([]);
    expect(component.contasFiltradas.length).toBe(3);
  });

  /**
   * Teste de Alternância de Visualização.
   */
  it('deve alternar entre visualização de Cards e Tabela', () => {
    expect(component.visualizacaoEmCards).toBe(true);

    component.visualizacaoEmCards = false;
    fixture.detectChanges();

    expect(component.visualizacaoEmCards).toBe(false);
  });

  /**
   * Teste do Fluxo de Adicionar.
   */
  it('deve abrir dialog de cadastro e atualizar lista ao confirmar', () => {
    const novaConta = { ...listaContasMock[0], id: '99', nome: 'Nova Conta' };

    dialogServiceSpy.abrirFormularioConta.mockReturnValue(of(novaConta));

    component.aoAdicionar();

    expect(dialogServiceSpy.abrirFormularioConta).toHaveBeenCalledWith('cadastrar');
    expect(contaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  it('não deve atualizar lista se o cadastro for cancelado', () => {
    dialogServiceSpy.abrirFormularioConta.mockReturnValue(of(undefined));

    component.aoAdicionar();

    expect(dialogServiceSpy.abrirFormularioConta).toHaveBeenCalledWith('cadastrar');
    expect(contaServiceSpy.listar).toHaveBeenCalledTimes(1);
  });

  /**
   * Teste do Fluxo de Editar.
   */
  it('deve abrir dialog de edição e atualizar lista ao confirmar', () => {
    const contaAlvo = listaContasMock[0];
    const contaEditada = { ...contaAlvo, nome: 'Editada' };

    dialogServiceSpy.abrirFormularioConta.mockReturnValue(of(contaEditada));

    component.aoEditar(contaAlvo);

    expect(dialogServiceSpy.abrirFormularioConta).toHaveBeenCalledWith('editar', contaAlvo);
    expect(contaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  /**
   * Teste do Fluxo de Excluir.
   */
  it('deve abrir dialog de exclusão e atualizar lista ao confirmar', () => {
    const contaAlvo = listaContasMock[1];

    dialogServiceSpy.abrirFormularioConta.mockReturnValue(of(true));

    component.aoExcluir(contaAlvo);

    expect(dialogServiceSpy.abrirFormularioConta).toHaveBeenCalledWith('excluir', contaAlvo);
    expect(contaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });
});
