import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Componente estrutural de Layout (Grid System).
 * Responsável por organizar os cards filhos de forma responsiva:
 * - Mobile: 1 coluna
 * - Tablet: 2 colunas
 * - Desktop: 3 colunas
 * - Wide: 4 colunas
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-grid-base',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './grid-base.html',
  styleUrl: './grid-base.scss',
})
export class GridBase {
  @Input() identificador: string = 'grid-principal';
}
