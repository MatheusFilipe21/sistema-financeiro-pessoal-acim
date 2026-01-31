import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Botão Flutuante de Ação (FAB) para adicionar novos registros.
 *
 * Ajusta automaticamente sua posição vertical se houver um rodapé fixo na tela.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-botao-adicionar',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './botao-adicionar.html',
  styleUrl: './botao-adicionar.scss',
})
export class BotaoAdicionar {
  /**
   * Define se o botão deve subir para dar espaço a um rodapé (ex: Paginador).
   * - true: Sobe para ~90px.
   * - false: Fica na posição padrão ~24px.
   */
  @Input() temRodape = false;

  /**
   * Texto explicativo. Se não for passado, usa o padrão.
   */
  @Input() tooltip = 'Adicionar novo registro';

  /**
   * Evento disparado quando o usuário clica no botão.
   * Deve ser capturado pelo componente pai para iniciar o fluxo de cadastro.
   */
  @Output() adicionar = new EventEmitter<void>();

  /**
   * Handler do clique no elemento DOM.
   * Repassa a ação para o componente pai via Output.
   */
  aoClicar() {
    this.adicionar.emit();
  }

  /**
   * Atalho do teclado
   * Captura a tecla INSERT para abrir o formulário de cadastro.
   */
  @HostListener('window:keydown', ['$event'])
  lidarComAtalho(event: KeyboardEvent) {
    if (event.key === 'Insert') {
      event.preventDefault();
      this.aoClicar();
    }
  }
}
