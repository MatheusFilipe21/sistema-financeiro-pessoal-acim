import { ErrorHandler, Injectable, Injector, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErroPadraoDTO } from '../dtos/erro/ErroPadraoDTO';
import { ErroValidacaoDTO } from '../dtos/erro/ErroValidacaoDTO';
import { DadosErroDialog } from '../components/dialogs/erro-dialog/erro-dialog';
import { Dialog as DialogService } from '../services/dialog';

/**
 * Handler (Manipulador) Global de Exceções.
 * Esta classe (registrada no app.config.ts) intercepta todos os erros
 * não tratados da aplicação (Client-side e Server-side) e
 * exibe um modal (Dialog) de erro padronizado (RF05).
 *
 * @author Matheus F. N. Pereira
 */
@Injectable()
export class TratadorDeErrosGlobal implements ErrorHandler {
  /**
   * Construtor do Handler.
   * Utiliza o {@link Injector} para obter dependências "lazy" (tardiamente).
   * Isso é necessário porque o ErrorHandler é inicializado muito cedo
   * no ciclo de vida da aplicação, antes de serviços como MatDialog.
   *
   * @param injector O injetor de dependências do Angular.
   */
  constructor(private readonly injector: Injector) {}

  /**
   * Método principal que captura e trata o erro.
   * Este método é chamado automaticamente pelo framework Angular
   * sempre que uma exceção não é tratada.
   *
   * @param error O erro (pode ser HttpErrorResponse ou um Error).
   */
  handleError(error: any): void {
    const dialogService = this.injector.get(DialogService);
    const zone = this.injector.get(NgZone);

    const dadosDialog = this.processarErro(error);

    console.error('[TratadorDeErrosGlobal] Erro capturado:', error);

    zone.run(() => {
      dialogService.mostrarErro(dadosDialog);
    });
  }

  /**
   * Processa o erro (HTTP ou Client-side) e o converte
   * para a estrutura de dados (DialogErroDados) que o modal espera.
   *
   * @param error O erro capturado.
   * @returns Os dados formatados para o Dialog.
   */
  private processarErro(error: any): DadosErroDialog {
    if (error instanceof HttpErrorResponse) {
      return this.processarHttpError(error);
    }

    if (error instanceof Error) {
      return this.criarErroPadrao('Erro na Aplicação', error.message);
    }

    return this.criarErroPadrao('Erro Inesperado', 'Ocorreu um erro na aplicação.');
  }

  /**
   * Processa um erro originado do HttpClient (HttpErrorResponse).
   * Este método analisa o corpo do erro (error.error) para determinar se
   * o Backend enviou um JSON de erro padronizado (ErroPadraoDTO ou
   * ErroValidacaoDTO) ou se foi um erro de rede/servidor (como 500 ou 0).
   *
   * @param error O objeto HttpErrorResponse capturado.
   * @returns Os dados formatados ({@link DialogErroDados}) para o Dialog.
   */
  private processarHttpError(error: HttpErrorResponse): DadosErroDialog {
    const erroApi = error.error;

    if (erroApi?.erro && Array.isArray(erroApi.erros)) {
      const erroValidacao = erroApi as ErroValidacaoDTO;

      const errosOrdenados = [...erroValidacao.erros].sort((a, b) =>
        a.campo.localeCompare(b.campo)
      );

      return {
        erroPadrao: erroValidacao.erro,
        listaErros: errosOrdenados,
      };
    }

    if (erroApi?.status && erroApi.titulo) {
      return {
        erroPadrao: erroApi as ErroPadraoDTO,
        listaErros: [],
      };
    }

    const status = error.status;
    const rota = error.url || 'Desconhecida';
    let mensagem = `Ocorreu um erro ao acessar o servidor (Status: ${status}).`;

    if (status === 0) {
      mensagem = 'Não foi possível conectar ao servidor. Verifique sua rede.';
    }

    return this.criarErroPadrao('Erro de Comunicação', mensagem, rota, status);
  }

  /**
   * Helper (auxiliar) para criar um ErroPadraoDTO genérico.
   *
   * @param titulo Título do erro (ex: "Erro de Rede").
   * @param mensagem Mensagem principal do erro.
   * @param rota A rota onde o erro ocorreu.
   * @param status O status HTTP (padrão 500).
   * @returns Os dados formatados ({@link DadosErroDialog}) para o Dialog.
   */
  private criarErroPadrao(
    titulo: string,
    mensagem: string,
    rota: string = 'Desconhecida',
    status: number = 500
  ): DadosErroDialog {
    return {
      erroPadrao: {
        status: status,
        titulo: titulo,
        mensagem: mensagem,
        dataHora: new Date()
          .toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            timeZone: 'America/Sao_Paulo',
          })
          .replace(',', ''),
        rota: rota,
      },
      listaErros: [],
    };
  }
}
