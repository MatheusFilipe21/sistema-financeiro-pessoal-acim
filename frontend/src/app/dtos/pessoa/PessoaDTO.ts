/**
 * DTO (Interface) que espelha os dados públicos de uma Pessoa
 * retornados pelo Backend (RF46, RF47).
 *
 * @author Matheus F. N. Pereira
 */
export interface PessoaDTO {
  /**
   * O identificador único (UUID) da pessoa.
   */
  id: string;

  /**
   * O nome da pessoa.
   */
  nome: string;

  /**
   * Indica se a pessoa pode ser titular de contas.
   */
  titular: boolean;
}
