import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { CategoriaDialog, DadosCategoriaDialog } from './categoria-dialog';
import { Categoria as CategoriaService } from '../../../services/categoria';
import { CategoriaDTO } from '../../../dtos/categoria/CategoriaDTO';
import { TipoCategoria } from '../../../enums/TipoCategoria';

/**
 * Testes unitários para o componente {@link CategoriaDialog}.
 *
 * Valida a lógica de inicialização (títulos, preenchimento), validação de formulário,
 * seleção de cores/ícones e chamadas aos serviços de persistência.
 *
 * @author Matheus F. N. Pereira
 */
describe('CategoriaDialog', () => {
  let component: CategoriaDialog;
  let fixture: ComponentFixture<CategoriaDialog>;
  let categoriaServiceSpy: jasmine.SpyObj<CategoriaService>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<CategoriaDialog>>;

  /**
   * Função auxiliar para recriar o componente com dados específicos (Injection Token).
   * Isso permite testar cenários de Cadastro, Edição e Exclusão sem describes aninhados.
   */
  async function iniciarComponente(dados: DadosCategoriaDialog) {
    TestBed.resetTestingModule();

    categoriaServiceSpy = jasmine.createSpyObj('CategoriaService', [
      'cadastrar',
      'atualizar',
      'excluir',
    ]);
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CategoriaDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dados },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: CategoriaService, useValue: categoriaServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoriaDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  /**
   * Testa a inicialização padrão (Cadastro).
   */
  it('deve iniciar em modo Cadastro (título "Nova Categoria" e valores padrão)', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    expect(component.titulo).toBe('Nova Categoria');
    expect(component.operacao).toBe('cadastrar');
    expect(component.formulario.get('id')?.value).toBeNull();
    expect(component.formulario.get('nome')?.value).toBe('');

    expect(component.formulario.get('icone')?.value).toBe('category');
    expect(component.formulario.get('cor')?.value).toBe('#F44336');
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

    const novaCategoria: CategoriaDTO = {
      id: '123',
      nome: 'Lazer',
      tipo: TipoCategoria.DESPESA,
      icone: 'sports_soccer',
      cor: '#FF0000',
      sistema: false,
    };

    component.formulario.patchValue({
      nome: 'Lazer',
      tipo: TipoCategoria.DESPESA,
      icone: 'sports_soccer',
      cor: '#FF0000',
    });

    categoriaServiceSpy.cadastrar.and.returnValue(of(novaCategoria));

    component.confirmarAcao();

    expect(categoriaServiceSpy.cadastrar).toHaveBeenCalledWith({
      nome: 'Lazer',
      tipo: TipoCategoria.DESPESA,
      icone: 'sports_soccer',
      cor: '#FF0000',
    });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(novaCategoria);
  });

  /**
   * Testa a inicialização em modo Edição.
   */
  it('deve iniciar em modo Edição e preencher formulário', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '123',
      nome: 'Salário',
      tipo: TipoCategoria.RECEITA,
      icone: 'attach_money',
      cor: '#00FF00',
      sistema: false,
    };
    await iniciarComponente({ acao: 'editar', categoria: categoriaExistente });

    expect(component.titulo).toBe('Editar Categoria');
    expect(component.formulario.value).toEqual({
      id: '123',
      nome: 'Salário',
      tipo: TipoCategoria.RECEITA,
      icone: 'attach_money',
      cor: '#00FF00',
    });
  });

  /**
   * Testa o fluxo de sucesso na Edição.
   */
  it('deve chamar serviço.atualizar e fechar dialog ao salvar edição', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '123',
      nome: 'Salário',
      tipo: TipoCategoria.RECEITA,
      icone: 'attach_money',
      cor: '#00FF00',
      sistema: false,
    };
    await iniciarComponente({ acao: 'editar', categoria: categoriaExistente });

    component.formulario.patchValue({ nome: 'Salário Mensal' });

    const categoriaAtualizada = { ...categoriaExistente, nome: 'Salário Mensal' };
    categoriaServiceSpy.atualizar.and.returnValue(of(categoriaAtualizada));

    component.confirmarAcao();

    expect(categoriaServiceSpy.atualizar).toHaveBeenCalledWith('123', {
      nome: 'Salário Mensal',
      tipo: TipoCategoria.RECEITA,
      icone: 'attach_money',
      cor: '#00FF00',
    });
    expect(dialogRefSpy.close).toHaveBeenCalledWith(categoriaAtualizada);
  });

  /**
   * Testa a inicialização em modo Exclusão.
   */
  it('deve iniciar em modo Exclusão e desabilitar formulário', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '999',
      nome: 'Deletar',
      tipo: TipoCategoria.DESPESA,
      icone: 'delete',
      cor: '#000000',
      sistema: false,
    };
    await iniciarComponente({ acao: 'excluir', categoria: categoriaExistente });

    expect(component.titulo).toBe('Excluir Categoria');
    expect(component.formulario.disabled).toBeTrue();
  });

  /**
   * Testa o fluxo de sucesso na Exclusão.
   */
  it('deve chamar serviço.excluir e fechar dialog retornando true', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '999',
      nome: 'Deletar',
      tipo: TipoCategoria.DESPESA,
      icone: 'delete',
      cor: '#000000',
      sistema: false,
    };
    await iniciarComponente({ acao: 'excluir', categoria: categoriaExistente });

    categoriaServiceSpy.excluir.and.returnValue(of(void 0));

    component.confirmarAcao();

    expect(categoriaServiceSpy.excluir).toHaveBeenCalledWith('999');
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

    const erro = component.obterMensagemErro('campo_fantasma');

    expect(erro).toBe('');
  });

  /**
   * Cobertura: return ''; (final do método)
   * Garante que retorna vazio quando o campo existe e está válido.
   */
  it('deve retornar vazio quando o controle é válido', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.formulario.get('nome')?.setValue('Categoria Válida');
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
    expect(categoriaServiceSpy.cadastrar).not.toHaveBeenCalled();
  });

  /**
   * Testa o helper selecionarIcone.
   */
  it('deve atualizar o valor do ícone no formulário ao chamar selecionarIcone', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.selecionarIcone('home');

    expect(component.formulario.get('icone')?.value).toBe('home');
  });

  /**
   * Testa a proteção do helper selecionarIcone em modo excluir.
   */
  it('não deve atualizar o ícone se estiver em modo excluir', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '999',
      nome: 'Teste',
      tipo: TipoCategoria.DESPESA,
      icone: 'work',
      cor: '#000',
      sistema: false,
    };
    await iniciarComponente({ acao: 'excluir', categoria: categoriaExistente });

    component.selecionarIcone('home');

    expect(component.formulario.get('icone')?.value).toBe('work');
  });

  /**
   * Testa o helper selecionarCor.
   */
  it('deve atualizar o valor da cor no formulário ao chamar selecionarCor', async () => {
    await iniciarComponente({ acao: 'cadastrar' });

    component.selecionarCor('#000000');

    expect(component.formulario.get('cor')?.value).toBe('#000000');
  });

  /**
   * Testa a proteção do helper selecionarCor em modo excluir.
   */
  it('não deve atualizar a cor se estiver em modo excluir', async () => {
    const categoriaExistente: CategoriaDTO = {
      id: '999',
      nome: 'Teste',
      tipo: TipoCategoria.DESPESA,
      icone: 'work',
      cor: '#FFFFFF',
      sistema: false,
    };
    await iniciarComponente({ acao: 'excluir', categoria: categoriaExistente });

    component.selecionarCor('#000000');

    expect(component.formulario.get('cor')?.value).toBe('#FFFFFF');
  });
});
