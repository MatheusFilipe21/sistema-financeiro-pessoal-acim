/**
 * DTO (Interface) que espelha os dados de resposta (JSON)
 * do Backend (RF12) após o login bem-sucedido.
 *
 * @author Matheus F. N. Pereira
 */
export interface DadosTokenJWTDTO {
  /**
   * O token JWT gerado (para ser armazenado no cliente).
   */
  token: string;
}
