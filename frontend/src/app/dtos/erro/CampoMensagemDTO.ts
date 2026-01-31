/**
 * DTO (Interface) que detalha um erro de validação específico
 * em um único campo do formulário.
 *
 * @author Matheus F. N. Pereira
 */
export interface CampoMensagemDTO {
  /**
   * O nome do campo que falhou na validação (ex: "senha").
   */
  campo: string;

  /**
   * A mensagem de erro específica para aquele campo (ex: "Este campo é obrigatório.").
   */
  mensagem: string;
}
