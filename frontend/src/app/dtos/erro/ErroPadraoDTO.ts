/**
 * DTO (Interface) que espelha a estrutura padrão de resposta
 * para erros da API (ex: 400, 404, 500).
 *
 * @author Matheus F. N. Pereira
 */
export interface ErroPadraoDTO {
  /**
   * O código de status HTTP do erro (ex: 400, 500).
   */
  status: number;

  /**
   * Um título breve para o erro (ex: "Violação de Dados").
   */
  titulo: string;

  /**
   * A mensagem detalhada explicando o erro (ex: "O e-mail já está cadastrado.").
   */
  mensagem: string;

  /**
   * A data e hora em que o erro ocorreu (formato: dd/MM/yyyy HH:mm).
   */
  dataHora: string;

  /**
   * A rota (URI) da API onde o erro foi acionado.
   */
  rota: string;
}
