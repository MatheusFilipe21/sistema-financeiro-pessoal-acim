import { Directive, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Diretiva responsável por formatar inputs numéricos para 2 casas decimais
 * ao perder o foco (blur).
 *
 * Uso: <input type="number" appFormatacaoDecimal ...>
 *
 * @author Matheus F. N. Pereira
 */
@Directive({
  selector: '[appFormatacaoDecimal]',
  standalone: true,
})
export class FormatacaoDecimal {
  private readonly ngControl = inject(NgControl, { optional: true });

  /**
   * Impede que a tecla "-" seja sequer inserida.
   */
  @HostListener('keydown', ['$event'])
  aoPressionarTecla(event: KeyboardEvent) {
    if (event.key === '-' || event.key === 'Minus' || event.key === 'e') {
      event.preventDefault();
    }
  }

  /**
   * Escuta a digitação para impedir número negativo e 3ª casa decimal em tempo real.
   */
  @HostListener('input', ['$event'])
  aoDigitar(event: Event) {
    const input = event.target as HTMLInputElement;
    let valor = input.value;

    let alterouValor = false;

    if (valor.includes('-')) {
      valor = valor.replaceAll('-', '');
      alterouValor = true;
    }

    if (valor.includes('.')) {
      const [inteiro, decimal] = valor.split('.');
      if (decimal && decimal.length > 2) {
        valor = `${inteiro}.${decimal.substring(0, 2)}`;
        alterouValor = true;
      }
    }

    if (alterouValor) {
      input.value = valor;
      this.ngControl?.control?.setValue(valor, { emitEvent: false });
    }
  }
  /**
   * Ao sair do campo, formata para garantir o padrão visual (ex: 10 -> 10.00).
   * Usa lógica de truncamento.
   */
  @HostListener('blur')
  aoPerderFoco() {
    if (!this.ngControl?.control) return;

    const valorAtual = this.ngControl.value;

    if (valorAtual !== null && valorAtual !== undefined && valorAtual !== '') {
      const valorString = valorAtual.toString();

      let valorFormatado: string;

      if (valorString.includes('.')) {
        const [inteiro, decimal] = valorString.split('.');
        const decimalTruncado = decimal.substring(0, 2);
        valorFormatado = `${inteiro}.${decimalTruncado.padEnd(2, '0')}`;
      } else {
        valorFormatado = `${valorString}.00`;
      }

      this.ngControl.control.setValue(valorFormatado, { emitEvent: false });
    }
  }
}
