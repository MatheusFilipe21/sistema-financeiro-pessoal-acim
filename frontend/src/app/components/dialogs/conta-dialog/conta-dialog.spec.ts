import { describe, it, expect, vi, Mocked } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { ContaDialog, DadosContaDialog } from './conta-dialog';
import { Conta as ContaService } from '../../../services/conta';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import { ContaDTO } from '../../../dtos/conta/ContaDTO';
import { InstituicaoFinanceira } from '../../../enums/InstituicaoFinanceira';

/**
 * Testes unitários para o componente {@link ContaDialog}.
 *
 * Valida a lógica de inicialização (carregamento de pessoas, filtragem de titulares),
 * preenchimento de formulário, formatação de valores monetários e
 * chamadas aos serviços de persistência.
 *
 * @author Matheus F. N. Pereira
 */
describe('ContaDialog', () => {
  let component: ContaDialog;
  let fixture: ComponentFixture<ContaDialog>;
  let contaServiceSpy: Mocked<ContaService>;
  let pessoaServiceSpy: Mocked<PessoaService>;
  let dialogRefSpy: Mocked<MatDialogRef<ContaDialog>>;

  const mockPessoaTitular: PessoaDTO = { id: 'p1', nome: 'Titular', titular: true };
  const mockPessoaDependente: PessoaDTO = { id: 'p2', nome: 'Dependente', titular: false };
  const mockConta: ContaDTO = {
    id: 'c1',
    nome: 'Conta Mercado Pago',
    instituicao: InstituicaoFinanceira.MERCADO_PAGO,
    saldoInicial: 150.5,
    saldoAtual: 150.5,
    pessoa: mockPessoaTitular,
  };

  /**
   * Função auxiliar para recriar o componente com dados específicos.
   * Configura também o mock do PessoaService para garantir que o ngOnInit não falhe.
   */
  async function iniciarComponente(dados: DadosContaDialog, pessoasMock = [mockPessoaTitular]) {
    TestBed.resetTestingModule();

    contaServiceSpy = {
      cadastrar: vi.fn(),
      atualizar: vi.fn(),
      excluir: vi.fn(),
    } as unknown as Mocked<ContaService>;

    pessoaServiceSpy = {
      listar: vi.fn(),
    } as unknown as Mocked<PessoaService>;

    dialogRefSpy = {
      close: vi.fn(),
    } as unknown as Mocked<MatDialogRef<ContaDialog>>;

    pessoaServiceSpy.listar.mockReturnValue(of(pessoasMock));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, ContaDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dados },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: ContaService, useValue: contaServiceSpy },
        { provide: PessoaService, useValue: pessoaServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  /**
   * Cenário: Inicialização e Carregamento de Dados
   */
  it('deve carregar pessoas e FILTRAR apenas os titulares no ngOnInit', async () => {
    await iniciarComponente({ acao: 'cadastrar' }, [mockPessoaTitular, mockPessoaDependente]);

    expect(component.pessoas.length).toBe(1);
    expect(component.pessoas[0].id).toBe('p1');
    expect(component.pessoas[0].titular).toBe(true);
  });

  it('deve iniciar em modo Cadastro (título "Nova Conta" e form vazio)', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    expect(component.titulo).toBe('Nova Conta');
    expect(component.operacao).toBe('cadastrar');
    expect(component.formulario.get('id')?.value).toBeNull();
    expect(component.formulario.get('nome')?.value).toBe('');
  });

  it('deve ordenar a lista de pessoas alfabeticamente pelo nome', async () => {
    const pessoaZ = { id: '3', nome: 'Zélia', titular: true };
    const pessoaA = { id: '1', nome: 'Ana', titular: true };
    const pessoaC = { id: '2', nome: 'Carlos', titular: true };

    await iniciarComponente({ acao: 'cadastrar' }, [pessoaZ, pessoaA, pessoaC]);

    expect(component.pessoas[0].nome).toBe('Ana');
    expect(component.pessoas[1].nome).toBe('Carlos');
    expect(component.pessoas[2].nome).toBe('Zélia');
  });

  /**
   * Cenário: Inicialização em Edição
   */
  it('deve iniciar em modo Edição, preencher form e formatar saldo (150.5 -> "150.50")', async () => {
    await iniciarComponente({ acao: 'editar', conta: mockConta });

    expect(component.titulo).toBe('Editar Conta');

    expect(component.formulario.get('id')?.value).toBe('c1');
    expect(component.formulario.get('nome')?.value).toBe('Conta Mercado Pago');
    expect(component.formulario.get('instituicao')?.value).toBe(InstituicaoFinanceira.MERCADO_PAGO);

    expect(component.formulario.get('pessoaId')?.value).toBe('p1');

    expect(component.formulario.get('saldoInicial')?.value).toBe('150.50');
  });

  it('deve formatar saldo como "0.00" se vier undefined na edição', async () => {
    const contaSemSaldo = { ...mockConta, saldoInicial: undefined } as any;
    await iniciarComponente({ acao: 'editar', conta: contaSemSaldo });

    expect(component.formulario.get('saldoInicial')?.value).toBe('0.00');
  });

  /**
   * Cenário: Inicialização em Exclusão
   */
  it('deve iniciar em modo Exclusão e desabilitar formulário', async () => {
    await iniciarComponente({ acao: 'excluir', conta: mockConta });

    expect(component.titulo).toBe('Excluir Conta');
    expect(component.formulario.disabled).toBe(true);
  });

  /**
   * Cenário: Tratamento de Erro no Carregamento
   */
  it('deve configurar o dialog mesmo se falhar ao carregar pessoas', async () => {
    TestBed.resetTestingModule();
    contaServiceSpy = { cadastrar: vi.fn() } as any;
    pessoaServiceSpy = { listar: vi.fn() } as any;
    dialogRefSpy = { close: vi.fn() } as any;

    pessoaServiceSpy.listar.mockReturnValue(throwError(() => new Error('Erro API')));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, ContaDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { acao: 'cadastrar' } },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: ContaService, useValue: contaServiceSpy },
        { provide: PessoaService, useValue: pessoaServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaDialog);
    component = fixture.componentInstance;

    expect(() => fixture.detectChanges()).not.toThrow();

    expect(component.titulo).toBe('Nova Conta');
  });

  /**
   * Cenário: Validação de Campos
   */
  it('deve retornar mensagem de erro para campos obrigatórios vazios', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    const campos = ['nome', 'instituicao', 'pessoaId', 'saldoInicial'];

    campos.forEach((campo) => {
      const controle = component.formulario.get(campo);
      controle?.setValue('');
      controle?.markAsTouched();
      expect(component.obterMensagemErro(campo)).toBe('Este campo é obrigatório.');
    });
  });

  it('deve retornar string vazia se o controle for válido ou inexistente', async () => {
    await iniciarComponente({ acao: 'cadastrar' });
    expect(component.obterMensagemErro('campo_fantasma')).toBe('');

    component.formulario.get('nome')?.setValue('Teste');
    expect(component.obterMensagemErro('nome')).toBe('');
  });

  /**
   * Cenário: Salvar (Cadastro)
   */
  it('deve chamar cadastrar e fechar dialog com sucesso', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.formulario.patchValue({
      nome: 'Itaú',
      instituicao: 'ITAU',
      pessoaId: 'p1',
      saldoInicial: '100.00',
    });

    contaServiceSpy.cadastrar.mockReturnValue(of(mockConta));

    component.confirmarAcao();

    expect(contaServiceSpy.cadastrar).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Itaú',
        instituicao: 'ITAU',
        pessoaId: 'p1',
      }),
    );
    expect(dialogRefSpy.close).toHaveBeenCalledWith(mockConta);
  });

  /**
   * Cenário: Salvar (Edição)
   */
  it('deve chamar atualizar e fechar dialog com sucesso', async () => {
    await iniciarComponente({ acao: 'editar', conta: mockConta });

    component.formulario.patchValue({ nome: 'Mercado Pago Atualizado' });

    const contaAtualizada = { ...mockConta, nome: 'Mercado Pago Atualizado' };
    contaServiceSpy.atualizar.mockReturnValue(of(contaAtualizada));

    component.confirmarAcao();

    expect(contaServiceSpy.atualizar).toHaveBeenCalledWith(
      'c1',
      expect.objectContaining({
        nome: 'Mercado Pago Atualizado',
      }),
    );
    expect(dialogRefSpy.close).toHaveBeenCalledWith(contaAtualizada);
  });

  /**
   * Cenário: Tentativa de Salvar Inválida
   */
  it('não deve chamar serviço se formulário inválido', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    vi.spyOn(component.formulario, 'markAllAsTouched');

    component.confirmarAcao();

    expect(component.formulario.markAllAsTouched).toHaveBeenCalled();
    expect(contaServiceSpy.cadastrar).not.toHaveBeenCalled();
  });

  /**
   * Cenário: Exclusão
   */
  it('deve chamar excluir e fechar dialog retornando true', async () => {
    await iniciarComponente({ acao: 'excluir', conta: mockConta });

    contaServiceSpy.excluir.mockReturnValue(of(void 0));

    component.confirmarAcao();

    expect(contaServiceSpy.excluir).toHaveBeenCalledWith('c1');
    expect(dialogRefSpy.close).toHaveBeenCalledWith(true);
  });

  /**
   * Cenário: Cancelar
   */
  it('deve fechar o dialog sem retornar dados ao cancelar', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.fechar();

    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });
});
