import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { map } from 'rxjs/operators';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

import { Autenticacao as AutenticacaoService } from '../../services/autenticacao';
import { toSignal } from '@angular/core/rxjs-interop';

/**
 * Componente estrutural responsável pelo layout administrativo (Sidenav + Toolbar).
 * Gerencia a responsividade e a navegação principal.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard {
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly autenticacaoService = inject(AutenticacaoService);

  /**
   * (RNF15) Signal que define se o layout deve se comportar como Mobile.
   * * Regra de Negócio Visual:
   * - TRUE (Mobile): Celulares (XSmall) e Tablets em Retrato (Small).
   * - FALSE (Desktop): Tablets em Paisagem, Laptops e Monitores (Medium, Large, XLarge).
   * * Limite de corte: 960px.
   */
  dispositivoMovel = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Handset])
      .pipe(map((result) => result.matches)),
    { initialValue: false }
  );

  /**
   * Realiza o logout chamando o serviço e limpando a sessão (RF26).
   */
  deslogar(): void {
    this.autenticacaoService.deslogar();
  }
}
