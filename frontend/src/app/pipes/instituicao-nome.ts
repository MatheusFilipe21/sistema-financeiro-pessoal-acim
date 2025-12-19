import { Pipe, PipeTransform } from '@angular/core';
import { InstituicaoFinanceira, InstituicaoFinanceiraNome } from '../enums/InstituicaoFinanceira';

/**
 * Pipe responsável por converter o Enum de Instituição Financeira em seu nome de exibição amigável.
 *
 * @example
 * {{ conta.instituicao | instituicaoNome }}
 *
 * @author Matheus F. N. Pereira
 */
@Pipe({
  name: 'instituicaoNome',
  standalone: true,
})
export class InstituicaoNome implements PipeTransform {
  /**
   * Transforma o código da instituição em seu rótulo textual.
   *
   * @param valor O Enum da instituição.
   * @returns O nome formatado ou 'Desconhecido' caso não encontrado.
   */
  transform(valor: InstituicaoFinanceira): string {
    return InstituicaoFinanceiraNome[valor] || 'Desconhecido';
  }
}
