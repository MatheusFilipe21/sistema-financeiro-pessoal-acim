/**
 * DTO (Interface) que espelha os dados de requisição (JSON)
 * para o endpoint de redefinição de senha (POST /autenticacao/redefinir-senha).
 *
 * @author Matheus F. N. Pereira
 */
export interface DadosRedefinicaoSenhaDTO {
  /**
   * Token JWT de recuperação recebido via e-mail e extraído da URL.
   */
  token: string;

  /**
   * RF17: A nova senha definida pelo usuário.
   */
  senha: string;
}
