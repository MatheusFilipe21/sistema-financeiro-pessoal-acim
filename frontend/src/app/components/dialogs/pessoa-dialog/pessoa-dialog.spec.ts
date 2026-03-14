import { describe, it, expect, vi, Mocked } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { PessoaDialog, DadosPessoaDialog } from './pessoa-dialog';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import { Dialog as DialogService } from '../../../services/dialog';

/**
 * Testes unitários para o componente {@link PessoaDialog}.
 *
 * Valida a lógica de inicialização (títulos, preenchimento), validação de formulário
 * e chamadas aos serviços de persistência (Cadastrar, Atualizar, Excluir).
 *
 * @author Matheus F. N. Pereira
 */
describe('PessoaDialog', () => {
  let component: PessoaDialog;
  let fixture: ComponentFixture<PessoaDialog>;
  let pessoaServiceSpy: Mocked<PessoaService>;
  let dialogRefSpy: Mocked<MatDialogRef<PessoaDialog>>;
  let dialogServiceSpy: Mocked<DialogService>;

  /**
   * Função auxiliar para recriar o componente com dados específicos (Injection Token).
   * Isso permite testar cenários de Cadastro, Edição e Exclusão sem describes aninhados.
   */
  async function iniciarComponente(dados: DadosPessoaDialog) {
    TestBed.resetTestingModule();

    pessoaServiceSpy = {
      cadastrar: vi.fn(),
      atualizar: vi.fn(),
      excluir: vi.fn(),
    } as unknown as Mocked<PessoaService>;

    dialogRefSpy = {
      close: vi.fn(),
    } as unknown as Mocked<MatDialogRef<PessoaDialog>>;

    dialogServiceSpy = {
      mostrarInfo: vi.fn(),
    } as unknown as Mocked<DialogService>;

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, PessoaDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dados },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: PessoaService, useValue: pessoaServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PessoaDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  /**
   * Testa a inicialização padrão (Cadastro).
   */
  it('deve iniciar em modo Cadastro (título "Nova Pessoa" e form vazio)', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    expect(component.titulo).toBe('Nova Pessoa');
    expect(component.operacao).toBe('cadastrar');
    expect(component.formulario.get('id')?.value).toBeNull();
    expect(component.formulario.get('nome')?.value).toBe('');
    expect(component.formulario.get('titular')?.value).toBe(false);
  });

  /**
   * Testa a validação de campos obrigatórios.
   */
  it('deve retornar mensagem de erro para nome vazio', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    const controle = component.formulario.get('nome');
    controle?.setValue('');
    controle?.markAsTouched();

    expect(component.obterMensagemErro('nome')).toBe('Este campo é obrigatório.');
  });

  /**
   * Testa o fluxo de sucesso no Cadastro.
   */
  it('deve chamar serviço.cadastrar e fechar dialog ao salvar novo registro', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    const novaPessoa: PessoaDTO = { id: '123', nome: 'Matheus', titular: true };
    component.formulario.patchValue({ nome: 'Matheus', titular: true });
    pessoaServiceSpy.cadastrar.mockReturnValue(of(novaPessoa));

    component.confirmarAcao();

    expect(pessoaServiceSpy.cadastrar).toHaveBeenCalledWith({ nome: 'Matheus', titular: true });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(novaPessoa);
  });

  /**
   * Testa a inicialização em modo Edição.
   */
  it('deve iniciar em modo Edição e preencher formulário', async () => {
    const pessoaExistente: PessoaDTO = { id: '123', nome: 'João', titular: true };
    await iniciarComponente({ acao: 'editar', pessoa: pessoaExistente });

    expect(component.titulo).toBe('Editar Pessoa');
    expect(component.formulario.value).toEqual({ id: '123', nome: 'João', titular: true });
  });

  /**
   * Testa o fluxo de sucesso na Edição.
   */
  it('deve chamar serviço.atualizar e fechar dialog ao salvar edição', async () => {
    const pessoaExistente: PessoaDTO = { id: '123', nome: 'João', titular: true };
    await iniciarComponente({ acao: 'editar', pessoa: pessoaExistente });

    component.formulario.patchValue({ nome: 'João Silva' });

    const pessoaAtualizada = { ...pessoaExistente, nome: 'João Silva' };
    pessoaServiceSpy.atualizar.mockReturnValue(of(pessoaAtualizada));

    component.confirmarAcao();

    expect(pessoaServiceSpy.atualizar).toHaveBeenCalledWith('123', {
      nome: 'João Silva',
      titular: true,
    });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(pessoaAtualizada);
  });

  /**
   * Testa a inicialização em modo Exclusão.
   */
  it('deve iniciar em modo Exclusão e desabilitar formulário', async () => {
    const pessoaExistente: PessoaDTO = { id: '999', nome: 'Deletar', titular: true };
    await iniciarComponente({ acao: 'excluir', pessoa: pessoaExistente });

    expect(component.titulo).toBe('Excluir Pessoa');
    expect(component.formulario.disabled).toBe(true);
  });

  /**
   * Testa o fluxo de sucesso na Exclusão.
   */
  it('deve chamar serviço.excluir e fechar dialog retornando true', async () => {
    const pessoaExistente: PessoaDTO = { id: '999', nome: 'Deletar', titular: true };
    await iniciarComponente({ acao: 'excluir', pessoa: pessoaExistente });

    pessoaServiceSpy.excluir.mockReturnValue(of(void 0));

    component.confirmarAcao();

    expect(pessoaServiceSpy.excluir).toHaveBeenCalledWith('999');
    expect(dialogRefSpy.close).toHaveBeenCalledWith(true);
  });

  /**
   * Testa a ação de fechar manualmente.
   */
  it('deve fechar o dialog sem retornar dados ao chamar fechar()', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.fechar();

    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });

  /**
   * Cobertura: if (!control) return '';
   * Garante que não quebra se pedir erro de um campo inexistente.
   */
  it('deve retornar string vazia se o controle não existir', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    const erro = component.obterMensagemErro('campo_fantasma_que_nao_existe');

    expect(erro).toBe('');
  });

  /**
   * Cobertura: return ''; (final do método)
   * Garante que retorna vazio quando o campo existe e está válido.
   */
  it('deve retornar vazio quando o controle é válido', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.formulario.get('nome')?.setValue('Nome Válido');
    component.formulario.get('nome')?.markAsTouched();

    const erro = component.obterMensagemErro('nome');

    expect(erro).toBe('');
  });

  /**
   * Cobertura: if (this.formulario.invalid) ... no método salvar()
   * Verifica se marca os campos como tocados (para mostrar erro visual) e aborta o envio.
   */
  it('não deve chamar serviço e deve marcar campos como tocados se formulário inválido', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.formulario.get('nome')?.setValue('');

    vi.spyOn(component.formulario, 'markAllAsTouched');

    component.confirmarAcao();

    expect(component.formulario.markAllAsTouched).toHaveBeenCalled();
    expect(pessoaServiceSpy.cadastrar).not.toHaveBeenCalled();
  });

  /**
   * Testa o método abrirInfoTitular.
   * Verifica se o serviço de dialog é chamado com o título e texto corretos.
   */
  it('deve abrir dialog informativo com a explicação correta ao chamar abrirInfoTitular', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.abrirInfoTitular();

    expect(dialogServiceSpy.mostrarInfo).toHaveBeenCalledWith(
      'Regra de Titularidade',
      expect.stringContaining('Apenas pessoas marcadas como <b>Titulares</b>'),
      'Entendi',
    );
  });
});
