import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MensagemDialog } from '../components/dialogs/mensagem-dialog/mensagem-dialog';
import { DadosErroDialog, ErroDialog } from '../components/dialogs/erro-dialog/erro-dialog';
import { ComponentType } from '@angular/cdk/overlay';
import { Observable } from 'rxjs';
import { PessoaDTO } from '../dtos/pessoa/PessoaDTO';
import { DadosPessoaDialog, PessoaDialog } from '../components/dialogs/pessoa-dialog/pessoa-dialog';
import { TipoOperacao } from '../components/dialogs/base-formulario-dialog/base-formulario-dialog';

/**
 * Serviço responsável por padronizar a abertura de dialogs
 * de feedback visual em toda a aplicação.
 *
 * Centraliza configurações como largura padrão, IDs para testes E2E
 * e a estrutura de dados passada para os componentes.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Dialog {
  private readonly dialog = inject(MatDialog);

  private readonly LARGURA_PADRAO = '400px';
  private readonly LARGURA_FORMULARIO = '500px';
  private readonly ID_ERRO = 'dialog-erro-global';
  private readonly ID_MENSAGEM = 'dialog-mensagem-global';
  private readonly ID_FORMULARIO_PESSOA = 'dialog-formulario-pessoa';

  /**
   * Abre o dialog de erro padronizado (ErroDialog).
   *
   * @param dados Os dados completos do erro.
   */
  mostrarErro(dados: DadosErroDialog): void {
    this.abrirDialog(ErroDialog, dados, this.LARGURA_PADRAO, this.ID_ERRO);
  }

  /**
   * Abre um dialog de sucesso (ícone verde).
   *
   * @param titulo O título do dialog.
   * @param mensagem A mensagem descritiva.
   * @param textoBotao O texto do botão de fechar.
   * @returns Um Observable que emite um valor quando o modal é fechado.
   */
  mostrarSucesso(titulo: string, mensagem: string, textoBotao: string = 'OK'): Observable<any> {
    return this.abrirDialog(
      MensagemDialog,
      {
        tipo: 'sucesso',
        titulo,
        mensagem,
        textoBotao,
      },
      this.LARGURA_PADRAO,
      this.ID_MENSAGEM
    );
  }

  /**
   * Abre um dialog de aviso (ícone amarelo).
   *
   * @param titulo O título do dialog.
   * @param mensagem A mensagem de alerta.
   * @param textoBotao O texto do botão de fechar.
   * @returns Um Observable que emite um valor quando o modal é fechado.
   */
  mostrarAviso(titulo: string, mensagem: string, textoBotao: string = 'Entendi'): Observable<any> {
    return this.abrirDialog(
      MensagemDialog,
      {
        tipo: 'aviso',
        titulo,
        mensagem,
        textoBotao,
      },
      this.LARGURA_PADRAO,
      this.ID_MENSAGEM
    );
  }

  /**
   * Abre um dialog informativo (ícone azul).
   *
   * @param titulo O título do dialog.
   * @param mensagem A informação a ser exibida.
   * @param textoBotao O texto do botão de fechar.
   * @returns Um Observable que emite um valor quando o modal é fechado.
   */
  mostrarInfo(titulo: string, mensagem: string, textoBotao: string = 'Fechar'): Observable<any> {
    return this.abrirDialog(
      MensagemDialog,
      {
        tipo: 'info',
        titulo,
        mensagem,
        textoBotao,
      },
      this.LARGURA_PADRAO,
      this.ID_MENSAGEM
    );
  }

  /**
   * Abre o formulário de gestão de Pessoa.
   *
   * @param acao O tipo de operação (cadastrar, editar, excluir).
   * @param pessoa (Opcional) O objeto para edição/exclusão.
   * @returns Observable que emite:
   * - PessoaDTO: se salvou uma inclusão/edição.
   * - boolean (true): se confirmou uma exclusão.
   * - undefined: se cancelou.
   */
  abrirFormularioPessoa(
    acao: TipoOperacao,
    pessoa?: PessoaDTO
  ): Observable<PessoaDTO | boolean | undefined> {
    const dados: DadosPessoaDialog = {
      acao: acao,
      pessoa: pessoa,
    };

    return this.abrirDialog(
      PessoaDialog,
      dados,
      this.LARGURA_FORMULARIO,
      this.ID_FORMULARIO_PESSOA
    );
  }

  /**
   * Método privado genérico que realiza a chamada ao MatDialog.
   *
   * @param componente O componente Angular a ser renderizado.
   * @param dados Os dados a serem injetados via MAT_DIALOG_DATA.
   * @param width A largura do dialog (ex: '400px').
   * @param id O ID HTML atribuído ao container do dialog (útil para testes E2E).
   * @returns Um Observable que emite um valor (ou completa) quando o modal é fechado.
   */
  private abrirDialog<T>(
    componente: ComponentType<T>,
    dados: any,
    width: string,
    id: string
  ): Observable<any> {
    const dialogJaAberto = this.dialog.getDialogById(id);

    if (dialogJaAberto) {
      return dialogJaAberto.afterClosed();
    }

    const dialogRef = this.dialog.open(componente, {
      width: width,
      data: dados,
      disableClose: true,
      autoFocus: 'first-tabbable',
      id: id,
    });

    return dialogRef.afterClosed();
  }
}
