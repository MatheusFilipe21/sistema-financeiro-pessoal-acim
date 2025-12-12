import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Componente "Wrapper" responsável por exibir os filtros da página.
 * Adapta a visualização entre layout horizontal (Desktop) e Sidenav/Gaveta (Mobile).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-container-filtro',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './container-filtro.html',
  styleUrl: './container-filtro.scss',
})
export class ContainerFiltro {
  private readonly breakpointObserver = inject(BreakpointObserver);

  @Input({ required: true }) titulo: string = 'Filtros';
  @Input() textoBotaoBuscar: string = 'Buscar';

  @Output() buscar = new EventEmitter<void>();
  @Output() limpar = new EventEmitter<void>();

  /**
   * (RNF22) Signal que define se o layout deve se comportar como Mobile.
   * Regra de visualização de filtros:
   * - TRUE: Exibe botão de funil que abre uma gaveta lateral.
   * - FALSE: Exibe os filtros expandidos em linha.
   */
  dispositivoMovel = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Handset])
      .pipe(map((result) => result.matches)),
    { initialValue: false }
  );
  filtroAberto = false;

  /**
   * Alterna a visibilidade da gaveta de filtros no mobile.
   */
  alternarFiltro(): void {
    this.filtroAberto = !this.filtroAberto;
  }

  /**
   * Fecha explicitamente a gaveta de filtros.
   */
  fecharFiltro(): void {
    this.filtroAberto = false;
  }

  /**
   * Emite o evento de busca e, se estiver no mobile, fecha a gaveta
   * para que o usuário veja os resultados imediatamente.
   */
  aoBuscar(): void {
    this.buscar.emit();

    if (this.dispositivoMovel()) {
      this.fecharFiltro();
    }
  }

  /**
   * Emite o evento de limpeza dos filtros.
   */
  aoLimpar(): void {
    this.limpar.emit();
  }
}
