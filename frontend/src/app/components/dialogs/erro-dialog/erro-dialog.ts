import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ErroPadraoDTO } from '../../../dtos/erro/ErroPadraoDTO';
import { CampoMensagemDTO } from '../../../dtos/erro/CampoMensagemDTO';

/**
 * Interface para os dados que o dialog de Erro espera receber.
 */
export interface DadosErroDialog {
  erroPadrao: ErroPadraoDTO;
  listaErros: CampoMensagemDTO[];
}

/**
 * Componente que renderiza o dialog de erro global.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-erro-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './erro-dialog.html',
  styleUrls: ['./erro-dialog.scss'],
})
export class ErroDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public dados: DadosErroDialog) {}
}
