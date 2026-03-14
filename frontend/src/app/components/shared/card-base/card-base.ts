import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

/**
 * Componente genérico de Cartão (Card).
 * Segue o padrão Material Design e inclui:
 * - Cabeçalho com Avatar/Ícone, Título e Subtítulo.
 * - Menu de ações padronizado (Editar/Excluir).
 * - Área de conteúdo livre (ng-content).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-card-base',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './card-base.html',
  styleUrl: './card-base.scss',
})
export class CardBase {
  @Input({ required: true }) identificador!: string | number;
  @Input({ required: true }) titulo: string = '';
  @Input() subtitulo: string = '';
  @Input() avatarUrl?: string;
  @Input() icone?: string;

  @Output() editar = new EventEmitter<void>();
  @Output() excluir = new EventEmitter<void>();

  /**
   * Dispara o evento de edição para que o componente pai trate a ação.
   */
  aoEditar() {
    this.editar.emit();
  }

  /**
   * Dispara o evento de exclusão para que o componente pai trate a ação.
   */
  aoExcluir() {
    this.excluir.emit();
  }
}
