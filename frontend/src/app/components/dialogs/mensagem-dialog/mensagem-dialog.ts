import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Define os tipos de feedback visual suportados pelo dialog.
 */
export type TipoMensagem = 'sucesso' | 'aviso' | 'info';

/**
 * Interface para os dados que o MensagemDialog espera receber.
 */
export interface DadosMensagem {
  tipo: TipoMensagem;
  titulo: string;
  mensagem: string;
  textoBotao?: string;
}

/**
 * Componente genérico para exibição de feedbacks visuais (Sucesso, Aviso, Info).
 * Diferente do ErroDialog, este componente foca em mensagens simples de negócio.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-mensagem-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './mensagem-dialog.html',
  styleUrls: ['./mensagem-dialog.scss'],
})
export class MensagemDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public dados: DadosMensagem) {}

  /**
   * Retorna o nome do ícone (Material Symbols) baseado no tipo da mensagem.
   *
   * @returns O nome do ícone ('check_circle', 'warning' ou 'info').
   */
  getIcone(): string {
    switch (this.dados.tipo) {
      case 'sucesso':
        return 'check_circle';
      case 'aviso':
        return 'warning';
      default:
        return 'info';
    }
  }
}
