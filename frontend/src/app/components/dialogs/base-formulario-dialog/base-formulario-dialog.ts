import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/**
 * Tipos de operação suportados pelo formulário base.
 * Define o comportamento dos botões e mensagens.
 */
export type TipoOperacao = 'cadastrar' | 'editar' | 'excluir';

/**
 * Componente "Casca" (Wrapper) para padronizar dialogs de formulário de entidades.
 *
 * Responsabilidades:
 * 1. Gerenciar o layout padrão (Cabeçalho, Conteúdo, Rodapé).
 * 2. Exibir alertas contextuais (ex: aviso de exclusão).
 * 3. Gerenciar estados de carregamento (spinner) e bloqueio de botões.
 * 4. Padronizar a posição e cor dos botões de ação (Salvar/Excluir/Cancelar).
 *
 * O conteúdo específico do formulário é injetado via `<ng-content>`.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-base-formulario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './base-formulario-dialog.html',
  styleUrl: './base-formulario-dialog.scss',
})
export class BaseFormularioDialog {
  /**
   * Título exibido no cabeçalho do modal.
   * Ex: "Nova Pessoa", "Editar Conta".
   */
  @Input({ required: true }) titulo!: string;

  /**
   * Define o modo de operação do dialog.
   * - `cadastrar` / `editar`: Exibe botão "Salvar" (Primary).
   * - `excluir`: Exibe alerta amarelo e botão "Excluir" (Warn).
   */
  @Input({ required: true }) operacao!: TipoOperacao;

  /**
   * Controla se o botão de confirmação deve estar desabilitado.
   * Geralmente ligado à validade do FormGroup (`form.invalid`).
   */
  @Input() formularioInvalido = false;

  /**
   * Estado de carregamento.
   * Se `true`:
   * - Desabilita todos os botões (X, Cancelar, Confirmar).
   * - Substitui o texto do botão de ação por um Spinner.
   */
  @Input() processando = false;

  /**
   * Evento disparado ao clicar no botão de ação principal (Salvar ou Excluir).
   * O componente pai deve ouvir este evento para submeter os dados.
   */
  @Output() confirmar = new EventEmitter<void>();

  /**
   * Evento disparado ao clicar em Cancelar ou no "X".
   * O componente pai deve fechar o dialog ao receber este evento.
   */
  @Output() cancelar = new EventEmitter<void>();

  /**
   * Retorna o texto do botão principal baseado na operação atual.
   */
  get textoBotaoConfirmar(): string {
    if (this.operacao === 'excluir') return 'Excluir';
    return this.operacao === 'editar' ? 'Salvar Alterações' : 'Salvar';
  }

  /**
   * Retorna a cor do botão principal (Material Palette).
   * - Excluir: `warn` (Vermelho/Laranja).
   * - Outros: `primary` (Cor principal do tema).
   */
  get corBotaoConfirmar(): 'primary' | 'warn' {
    return this.operacao === 'excluir' ? 'warn' : 'primary';
  }

  /**
   * Handler interno do clique de confirmação.
   * Só emite o evento se não estiver processando.
   */
  aoConfirmar() {
    if (!this.processando) {
      this.confirmar.emit();
    }
  }

  /**
   * Handler interno do clique de cancelamento.
   */
  aoCancelar() {
    this.cancelar.emit();
  }
}
