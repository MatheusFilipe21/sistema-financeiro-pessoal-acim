import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Componente reutilizável para alternar entre visualização de Cards e Lista (Tabela).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-alternador-visualizacao',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './alternador-visualizacao.html',
  styleUrl: './alternador-visualizacao.scss',
})
export class AlternadorVisualizacao {
  /**
   * Estado atual da visualização.
   * - true: Cards
   * - false: Lista
   */
  @Input() emCards = true;

  /**
   * Evento emitido quando o usuário clica para alternar.
   */
  @Output() emCardsChange = new EventEmitter<boolean>();

  /**
   * Alterna o estado de visualização e notifica o componente pai.
   */
  alternar() {
    this.emCards = !this.emCards;
    this.emCardsChange.emit(this.emCards);
  }
}
