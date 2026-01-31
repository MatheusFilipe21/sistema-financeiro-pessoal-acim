/**
 * DTO (Interface) que espelha os dados de resposta
 * do Backend (RF01) após o cadastro bem-sucedido.
 *
 * @author Matheus F. N. Pereira
 */
export interface UsuarioDTO {
  /**
   * O identificador único (UUID) do usuário.
   */
  id: string;

  /**
   * O nome completo do usuário.
   */
  nome: string;

  /**
   * O e-mail (único) do usuário.
   */
  email: string;
}
