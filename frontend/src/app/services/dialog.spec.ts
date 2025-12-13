import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { Dialog } from './dialog';
import { MensagemDialog } from '../components/dialogs/mensagem-dialog/mensagem-dialog';
import { ErroDialog, DadosErroDialog } from '../components/dialogs/erro-dialog/erro-dialog';

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
    const spy = jasmine.createSpyObj('MatDialog', ['open']);
    spy.open.and.returnValue(dialogRefSpy);

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
      autoFocus: false,
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
      autoFocus: false,
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
      autoFocus: false,
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
      autoFocus: false,
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
});
