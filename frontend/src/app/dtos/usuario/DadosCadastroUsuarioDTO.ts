/**
 * DTO (Interface) que espelha os dados de requisição
 * para o endpoint de cadastro (Backend).
 *
 * @author Matheus F. N. Pereira
 */
export interface DadosCadastroUsuarioDTO {
  /**
   * RF04: O nome é obrigatório.
   */
  nome: string;

  /**
   * RF02: O e-mail deve ter um formato válido.
   * RF04: O e-mail é obrigatório.
   */
  email: string;

  /**
   * RF03: A senha deve ser complexa.
   * RF04: A senha é obrigatória.
   */
  senha: string;
}
