import { Pipe, PipeTransform } from '@angular/core';
import { InstituicaoFinanceira, obterEstiloInstituicao } from '../enums/InstituicaoFinanceira';

/**
 * Pipe responsável por gerar o objeto de estilos CSS (Cores da Marca).
 * Utilizado para colorir badges, tags ou bordas de acordo com a identidade visual do banco.
 *
 * @example
 * <div [ngStyle]="conta.instituicao | instituicaoEstilo">...</div>
 *
 * @author Matheus F. N. Pereira
 */
@Pipe({
  name: 'instituicaoEstilo',
  standalone: true,
})
export class InstituicaoEstilo implements PipeTransform {
  /**
   * Gera o objeto de estilos para o Angular aplicar dinamicamente.
   *
   * @param valor O Enum da instituição.
   * @returns Um objeto contendo { 'background-color', 'color', 'border' }.
   */
  transform(valor: InstituicaoFinanceira): Record<string, string> {
    return obterEstiloInstituicao(valor);
  }
}
