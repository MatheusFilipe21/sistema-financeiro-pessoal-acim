/**
 * DTO (Interface) que espelha os dados de requisição
 * para criação e atualização de pessoas (Backend).
 *
 * <p>
 * Utilizado no corpo (body) dos métodos POST e PUT.
 *
 * @author Matheus F. N. Pereira
 */
export interface CriarAtualizarPessoaDTO {
  /**
   * O nome da pessoa.
   */
  nome: string;
}
