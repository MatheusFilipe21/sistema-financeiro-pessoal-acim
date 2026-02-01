import { TipoCategoria } from '../../enums/TipoCategoria';

/**
 * DTO (Interface) que espelha os dados públicos de uma Categoria
 * retornados pelo Backend.
 *
 * @author Matheus F. N. Pereira
 */
export interface CategoriaDTO {
  /**
   * Identificador único (UUID) da categoria.
   */
  id: string;

  /**
   * Nome da categoria.
   */
  nome: string;

  /**
   * Tipo da categoria (Receita, Despesa ou Ambos).
   */
  tipo: TipoCategoria;

  /**
   * Identificador do ícone visual (Material Icons).
   */
  icone: string;

  /**
   * Cor hexadecimal para exibição.
   */
  cor: string;

  /**
   * Indica se a categoria é padrão do sistema (não editável).
   */
  sistema: boolean;
}
