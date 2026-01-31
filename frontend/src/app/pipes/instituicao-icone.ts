import { Pipe, PipeTransform } from '@angular/core';
import { InstituicaoFinanceira, obterMetadadosInstituicao } from '../enums/InstituicaoFinanceira';

/**
 * Pipe responsável por recuperar o ícone (Material Symbol) associado à instituição.
 * Ideal para uso em tags <mat-icon> ou componentes que aceitam string de ícone.
 *
 * @example
 * <mat-icon>{{ conta.instituicao | instituicaoIcone }}</mat-icon>
 *
 * @author Matheus F. N. Pereira
 */
@Pipe({
  name: 'instituicaoIcone',
  standalone: true,
})
export class InstituicaoIcone implements PipeTransform {
  /**
   * Busca nos metadados o ícone visual da instituição.
   *
   * @param valor O Enum da instituição.
   * @returns A string correspondente ao ícone do Google Fonts/Material.
   */
  transform(valor: InstituicaoFinanceira): string {
    return obterMetadadosInstituicao(valor).icone;
  }
}
