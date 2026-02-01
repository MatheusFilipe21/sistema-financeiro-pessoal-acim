import { TipoCategoria } from '../../enums/TipoCategoria';

/**
 * DTO (Interface) que espelha os dados de requisição
 * para criação e atualização de categorias.
 *
 * @author Matheus F. N. Pereira
 */
export interface CriarAtualizarCategoriaDTO {
  /**
   * Nome da categoria (Obrigatório).
   */
  nome: string;

  /**
   * Tipo da categoria (Obrigatório).
   */
  tipo: TipoCategoria;

  /**
   * Ícone visual (Obrigatório).
   */
  icone: string;

  /**
   * Cor hexadecimal (Obrigatório, formato #RRGGBB).
   */
  cor: string;
}
