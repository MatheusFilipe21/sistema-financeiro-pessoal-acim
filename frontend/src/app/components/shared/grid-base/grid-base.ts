import { Component, Input } from '@angular/core';

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
  imports: [],
  templateUrl: './grid-base.html',
  styleUrl: './grid-base.scss',
})
export class GridBase {
  @Input() identificador: string = 'grid-principal';
}
