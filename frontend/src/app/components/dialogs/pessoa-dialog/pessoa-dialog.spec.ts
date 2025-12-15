import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { PessoaDialog, DadosPessoaDialog } from './pessoa-dialog';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';

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
  let pessoaServiceSpy: jasmine.SpyObj<PessoaService>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<PessoaDialog>>;

  /**
   * Função auxiliar para recriar o componente com dados específicos (Injection Token).
   * Isso permite testar cenários de Cadastro, Edição e Exclusão sem describes aninhados.
   */
  async function iniciarComponente(dados: DadosPessoaDialog) {
    TestBed.resetTestingModule();

    pessoaServiceSpy = jasmine.createSpyObj('PessoaService', ['cadastrar', 'atualizar', 'excluir']);
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, PessoaDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dados },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: PessoaService, useValue: pessoaServiceSpy },
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

    const novaPessoa: PessoaDTO = { id: '123', nome: 'Matheus' };
    component.formulario.patchValue({ nome: 'Matheus' });
    pessoaServiceSpy.cadastrar.and.returnValue(of(novaPessoa));

    component.confirmarAcao();

    expect(pessoaServiceSpy.cadastrar).toHaveBeenCalledWith({ nome: 'Matheus' });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(novaPessoa);
  });

  /**
   * Testa a inicialização em modo Edição.
   */
  it('deve iniciar em modo Edição e preencher formulário', async () => {
    const pessoaExistente: PessoaDTO = { id: '123', nome: 'João' };
    await iniciarComponente({ acao: 'editar', pessoa: pessoaExistente });

    expect(component.titulo).toBe('Editar Pessoa');
    expect(component.formulario.value).toEqual({ id: '123', nome: 'João' });
  });

  /**
   * Testa o fluxo de sucesso na Edição.
   */
  it('deve chamar serviço.atualizar e fechar dialog ao salvar edição', async () => {
    const pessoaExistente: PessoaDTO = { id: '123', nome: 'João' };
    await iniciarComponente({ acao: 'editar', pessoa: pessoaExistente });

    component.formulario.patchValue({ nome: 'João Silva' });

    const pessoaAtualizada = { ...pessoaExistente, nome: 'João Silva' };
    pessoaServiceSpy.atualizar.and.returnValue(of(pessoaAtualizada));

    component.confirmarAcao();

    expect(pessoaServiceSpy.atualizar).toHaveBeenCalledWith('123', { nome: 'João Silva' });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(pessoaAtualizada);
  });

  /**
   * Testa a inicialização em modo Exclusão.
   */
  it('deve iniciar em modo Exclusão e desabilitar formulário', async () => {
    const pessoaExistente: PessoaDTO = { id: '999', nome: 'Deletar' };
    await iniciarComponente({ acao: 'excluir', pessoa: pessoaExistente });

    expect(component.titulo).toBe('Excluir Pessoa');
    expect(component.formulario.disabled).toBeTrue();
  });

  /**
   * Testa o fluxo de sucesso na Exclusão.
   */
  it('deve chamar serviço.excluir e fechar dialog retornando true', async () => {
    const pessoaExistente: PessoaDTO = { id: '999', nome: 'Deletar' };
    await iniciarComponente({ acao: 'excluir', pessoa: pessoaExistente });

    pessoaServiceSpy.excluir.and.returnValue(of(void 0));

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

    spyOn(component.formulario, 'markAllAsTouched');

    component.confirmarAcao();

    expect(component.formulario.markAllAsTouched).toHaveBeenCalled();
    expect(pessoaServiceSpy.cadastrar).not.toHaveBeenCalled();
  });
});
