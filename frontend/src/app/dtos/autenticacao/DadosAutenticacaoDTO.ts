/**
 * DTO (Interface) que espelha os dados de requisição (JSON)
 * para o endpoint de login (POST /autenticacao/login).
 *
 * @author Matheus F. N. Pereira
 */
export interface DadosAutenticacaoDTO {
  /**
   * RF09: O e-mail é obrigatório e deve ser válido.
   */
  email: string;

  /**
   * RF09: A senha é obrigatória.
   */
  senha: string;
}
