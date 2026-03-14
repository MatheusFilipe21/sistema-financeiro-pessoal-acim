import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TabelaBase, ColunaTabela } from './tabela-base';
import { SimpleChange } from '@angular/core';

describe('TabelaBase', () => {
  let component: TabelaBase;
  let fixture: ComponentFixture<TabelaBase>;

  /** Definição de colunas simulada para os testes.
   * Representa o Input obrigatório 'colunas'.
   */
  const COLUNAS_MOCK: ColunaTabela[] = [
    { chave: 'nome', titulo: 'Nome Completo' },
    { chave: 'email', titulo: 'E-mail' },
  ];

  /** Massa de dados simulada para preencher a tabela.
   * Representa o Input obrigatório 'dados'.
   */
  const DADOS_MOCK = [
    { id: 1, nome: 'João', email: 'joao@teste.com' },
    { id: 2, nome: 'Maria', email: 'maria@teste.com' },
  ];

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [TabelaBase],
    }).compileComponents();

    fixture = TestBed.createComponent(TabelaBase);
    component = fixture.componentInstance;

    component.colunas = COLUNAS_MOCK;
    component.dados = DADOS_MOCK;

    component.ngOnChanges({
      colunas: new SimpleChange(null, COLUNAS_MOCK, true),
      dados: new SimpleChange(null, DADOS_MOCK, true),
    });

    fixture.detectChanges();
  });

  /**
   * Verifica a criação do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Teste de Renderização: Cabeçalhos.
   * Verifica se os títulos das colunas e a coluna de ações são renderizados.
   */
  it('deve renderizar os cabeçalhos das colunas corretamente', () => {
    const headerNome = fixture.nativeElement.querySelector('#cabecalho-nome');
    const headerEmail = fixture.nativeElement.querySelector('#cabecalho-email');
    const headerAcoes = fixture.nativeElement.querySelector('#cabecalho-acoes');

    expect(headerNome.textContent).toContain('Nome Completo');
    expect(headerEmail.textContent).toContain('E-mail');
    expect(headerAcoes).toBeTruthy();
  });

  /**
   * Teste de Renderização: Células de Dados.
   * Verifica se os valores (João, Maria) estão nas células corretas com IDs dinâmicos.
   */
  it('deve renderizar os dados nas células com IDs dinâmicos', () => {
    const celulaNome1 = fixture.nativeElement.querySelector('#celula-nome-1');
    const celulaEmail1 = fixture.nativeElement.querySelector('#celula-email-1');

    expect(celulaNome1.textContent).toContain('João');
    expect(celulaEmail1.textContent).toContain('joao@teste.com');
  });

  /**
   * Teste de Integração: Evento de Editar.
   * Verifica se ao clicar no botão, o objeto correto é emitido.
   */
  it('deve emitir evento "editar" com o objeto correto ao clicar no botão', () => {
    vi.spyOn(component.editar, 'emit');

    const btnEditarJoao = fixture.nativeElement.querySelector('#btn-editar-1');
    expect(btnEditarJoao).toBeTruthy();

    btnEditarJoao.click();

    expect(component.editar.emit).toHaveBeenCalledWith(DADOS_MOCK[0]);
  });

  /**
   * Teste de Integração: Evento de Excluir.
   * Verifica se ao clicar no botão, o objeto correto é emitido.
   */
  it('deve emitir evento "excluir" com o objeto correto ao clicar no botão', () => {
    vi.spyOn(component.excluir, 'emit');

    const btnExcluirMaria = fixture.nativeElement.querySelector('#btn-excluir-2');
    expect(btnExcluirMaria).toBeTruthy();

    btnExcluirMaria.click();

    expect(component.excluir.emit).toHaveBeenCalledWith(DADOS_MOCK[1]);
  });

  /**
   * Teste de Configuração: Paginação e Ordenação.
   * Garante que o MatPaginator e MatSort foram vinculados ao DataSource.
   */
  it('deve inicializar o Paginator e Sort no ngAfterViewInit', () => {
    expect(component.dataSource.paginator).toBeTruthy();
    expect(component.dataSource.sort).toBeTruthy();
  });

  /**
   * Cenário de Dados Vazios.
   * Simula a troca dos dados para um array vazio e verifica a mensagem de feedback.
   */
  it('deve exibir mensagem quando não houver registros', () => {
    component.dados = [];

    component.ngOnChanges({
      dados: {
        currentValue: [],
        previousValue: DADOS_MOCK,
        firstChange: false,
        isFirstChange: () => false,
      },
    });
    fixture.detectChanges();

    const msgSemDados = fixture.nativeElement.querySelector('#msg-sem-dados');
    expect(msgSemDados).toBeTruthy();
    expect(msgSemDados.textContent).toContain('Nenhum registro encontrado');
  });

  /**
   * Teste de Lógica Interna: Ordenação Personalizada (SortingDataAccessor).
   * Verifica se a tabela consegue ler propriedades aninhadas (ex: pessoa.nome) para ordenar.
   */
  it('deve configurar o sortingDataAccessor para ler valores aninhados', () => {
    const DADO_COMPLEXO = {
      id: 99,
      cargo: 'Dev',
      empresa: { nome: 'Google', dados: { pais: 'EUA' } },
    };

    component.colunas = [
      { chave: 'cargo', titulo: 'Cargo' },
      { chave: 'empresa', titulo: 'Empresa', caminhoOrdenacao: 'empresa.nome' },
      { chave: 'pais', titulo: 'País', caminhoOrdenacao: 'empresa.dados.pais' },
    ];
    component.dados = [DADO_COMPLEXO];

    component.ngOnChanges({
      dados: new SimpleChange(null, component.dados, true),
      colunas: new SimpleChange(null, component.colunas, true),
    });

    const accessor = component.dataSource.sortingDataAccessor;

    expect(accessor(DADO_COMPLEXO, 'cargo')).toBe('Dev');
    expect(accessor(DADO_COMPLEXO, 'empresa')).toBe('Google');
    expect(accessor(DADO_COMPLEXO, 'pais')).toBe('EUA');
  });

  it('deve retornar null (e não quebrar) ao tentar acessar propriedades de um objeto inexistente', () => {
    const clienteCompleto = {
      nome: 'Ana',
      endereco: { cidade: 'Rio de Janeiro' },
    };

    const clienteSemEndereco = {
      nome: 'Pedro',
      endereco: null,
    };

    component.colunas = [
      { chave: 'cidade', titulo: 'Cidade', caminhoOrdenacao: 'endereco.cidade' },
    ];
    component.dados = [clienteCompleto, clienteSemEndereco];

    component.ngOnChanges({
      dados: new SimpleChange(null, component.dados, true),
      colunas: new SimpleChange(null, component.colunas, true),
    });

    const acessorOrdenacao = component.dataSource.sortingDataAccessor;

    const resultadoSucesso = acessorOrdenacao(clienteCompleto, 'cidade');
    expect(resultadoSucesso).toBe('Rio de Janeiro');

    const resultadoNulo = acessorOrdenacao(clienteSemEndereco, 'cidade');
    expect(resultadoNulo).toBeNull();
  });

  /**
   * Verifica se os botões de ação respeitam a função condicional de bloqueio.
   */
  it('deve desabilitar os botões de ação quando a função desabilitarAcoes retornar true', () => {
    component.desabilitarAcoes = (item: any) => item.id === 1;

    component.ngOnChanges({
      dados: new SimpleChange(null, DADOS_MOCK, false),
    });

    fixture.detectChanges();

    const btnEditarJoao = fixture.nativeElement.querySelector('#btn-editar-1') as HTMLButtonElement;
    const btnExcluirJoao = fixture.nativeElement.querySelector(
      '#btn-excluir-1',
    ) as HTMLButtonElement;

    const btnEditarMaria = fixture.nativeElement.querySelector(
      '#btn-editar-2',
    ) as HTMLButtonElement;
    const btnExcluirMaria = fixture.nativeElement.querySelector(
      '#btn-excluir-2',
    ) as HTMLButtonElement;

    expect(btnEditarJoao.disabled).toBe(true);
    expect(btnExcluirJoao.disabled).toBe(true);

    expect(btnEditarMaria.disabled).toBe(false);
    expect(btnExcluirMaria.disabled).toBe(false);
  });
});
