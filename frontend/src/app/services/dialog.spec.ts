import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { Dialog } from './dialog';
import { MensagemDialog } from '../components/dialogs/mensagem-dialog/mensagem-dialog';
import { ErroDialog, DadosErroDialog } from '../components/dialogs/erro-dialog/erro-dialog';
import { DadosPessoaDialog, PessoaDialog } from '../components/dialogs/pessoa-dialog/pessoa-dialog';

/**
 * Testes unitários para o serviço {@link Dialog}.
 *
 * @author Matheus F. N. Pereira
 */
describe('Dialog', () => {
  let service: Dialog;
  let matDialogSpy: jasmine.SpyObj<MatDialog>;

  const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
  dialogRefSpy.afterClosed.and.returnValue(of(true));

  /**
   * Configura o ambiente de testes antes de cada 'it'.
   */
  beforeEach(() => {
    const spy = jasmine.createSpyObj('MatDialog', ['open', 'getDialogById']);
    spy.open.and.returnValue(dialogRefSpy);
    spy.getDialogById.and.returnValue(null);

    TestBed.configureTestingModule({
      providers: [Dialog, { provide: MatDialog, useValue: spy }],
    });

    service = TestBed.inject(Dialog);
    matDialogSpy = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
  });

  /**
   * Testa se o serviço é criado com sucesso.
   */
  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  /**
   * Testa o método mostrarErro().
   * Verifica se abre o ErroDialog com o ID 'dialog-erro-global' e largura padrão.
   */
  it('deve abrir o ErroDialog com as configurações corretas', () => {
    const dadosErro: DadosErroDialog = {
      erroPadrao: {
        status: 500,
        titulo: 'Erro',
        mensagem: 'Msg',
        dataHora: '',
        rota: '',
      },
      listaErros: [],
    };

    service.mostrarErro(dadosErro);

    expect(matDialogSpy.open).toHaveBeenCalledWith(ErroDialog, {
      width: '400px',
      data: dadosErro,
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-erro-global',
    });
  });

  /**
   * Testa o método mostrarSucesso().
   * Verifica se abre o MensagemDialog com tipo 'sucesso' e ID de mensagem.
   */
  it('deve abrir o MensagemDialog de sucesso corretamente', () => {
    service.mostrarSucesso('Título', 'Msg Sucesso', 'Botão');

    expect(matDialogSpy.open).toHaveBeenCalledWith(MensagemDialog, {
      width: '400px',
      data: {
        tipo: 'sucesso',
        titulo: 'Título',
        mensagem: 'Msg Sucesso',
        textoBotao: 'Botão',
      },
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-mensagem-global',
    });
  });

  /**
   * Testa o método mostrarAviso().
   * Verifica se abre o MensagemDialog com tipo 'aviso' e texto padrão do botão.
   */
  it('deve abrir o MensagemDialog de aviso corretamente', () => {
    service.mostrarAviso('Alerta', 'Msg Aviso');

    expect(matDialogSpy.open).toHaveBeenCalledWith(MensagemDialog, {
      width: '400px',
      data: {
        tipo: 'aviso',
        titulo: 'Alerta',
        mensagem: 'Msg Aviso',
        textoBotao: 'Entendi',
      },
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-mensagem-global',
    });
  });

  /**
   * Testa o método mostrarInfo().
   * Verifica se abre o MensagemDialog com tipo 'info' e texto padrão do botão.
   */
  it('deve abrir o MensagemDialog de informação corretamente', () => {
    service.mostrarInfo('Info', 'Msg Info');

    expect(matDialogSpy.open).toHaveBeenCalledWith(MensagemDialog, {
      width: '400px',
      data: {
        tipo: 'info',
        titulo: 'Info',
        mensagem: 'Msg Info',
        textoBotao: 'Fechar',
      },
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-mensagem-global',
    });
  });

  /**
   * Verifica se os métodos retornam o Observable do afterClosed.
   * Isso garante que os componentes que chamam o serviço possam reagir ao fechamento do modal.
   */
  it('deve retornar o observable afterClosed', (done) => {
    service.mostrarSucesso('T', 'M').subscribe((resultado) => {
      expect(resultado).toBeTrue();
      done();
    });
  });

  /**
   * Testa o método abrirFormularioPessoa() para inclusão.
   * Verifica se abre o PessoaDialog com a ação 'cadastrar' e sem dados de pessoa.
   */
  it('deve abrir o PessoaDialog para cadastro (ação: cadastrar)', () => {
    service.abrirFormularioPessoa('cadastrar');

    const expectedData: DadosPessoaDialog = {
      acao: 'cadastrar',
      pessoa: undefined,
    };

    expect(matDialogSpy.open).toHaveBeenCalledWith(PessoaDialog, {
      width: '500px',
      data: expectedData,
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-formulario-pessoa',
    });
  });

  /**
   * Testa o método abrirFormularioPessoa() para edição.
   * Verifica se passa o objeto pessoa e a ação 'editar' corretamente.
   */
  it('deve abrir o PessoaDialog para edição (ação: editar)', () => {
    const mockPessoa = { id: '123', nome: 'Teste', titular: true };

    service.abrirFormularioPessoa('editar', mockPessoa);

    const expectedData: DadosPessoaDialog = {
      acao: 'editar',
      pessoa: mockPessoa,
    };

    expect(matDialogSpy.open).toHaveBeenCalledWith(PessoaDialog, {
      width: '500px',
      data: expectedData,
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-formulario-pessoa',
    });
  });

  /**
   * Testa o método abrirFormularioPessoa() para exclusão.
   * Verifica se passa o objeto pessoa e a ação 'excluir'.
   */
  it('deve abrir o PessoaDialog para exclusão (ação: excluir)', () => {
    const mockPessoa = { id: '999', nome: 'Para Deletar', titular: true };

    service.abrirFormularioPessoa('excluir', mockPessoa);

    const expectedData: DadosPessoaDialog = {
      acao: 'excluir',
      pessoa: mockPessoa,
    };

    expect(matDialogSpy.open).toHaveBeenCalledWith(PessoaDialog, {
      width: '500px',
      data: expectedData,
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: 'dialog-formulario-pessoa',
    });
  });

  /**
   * Testa a blindagem contra IDs duplicados.
   * Cenário: Usuário aperta Insert/Botão várias vezes rápido.
   * Resultado: Deve retornar a referência do dialog já aberto e NÃO abrir um novo.
   */
  it('não deve abrir novo dialog se já existir um aberto com mesmo ID', (done) => {
    const dialogExistenteRef = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogExistenteRef.afterClosed.and.returnValue(of('retorno-do-dialog-anterior'));

    matDialogSpy.getDialogById.and.returnValue(dialogExistenteRef);

    service.abrirFormularioPessoa('cadastrar').subscribe((resultado) => {
      expect(resultado).toBe('retorno-do-dialog-anterior' as any);
      done();
    });

    expect(matDialogSpy.open).not.toHaveBeenCalled();
    expect(matDialogSpy.getDialogById).toHaveBeenCalledWith('dialog-formulario-pessoa');
  });
});
