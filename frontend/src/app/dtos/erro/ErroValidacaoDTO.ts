import { CampoMensagemDTO } from './CampoMensagemDTO';
import { ErroPadraoDTO } from './ErroPadraoDTO';

/**
 * DTO (Interface) que espelha a estrutura de resposta para erros
 * de validação (HTTP 422 - Unprocessable Entity).
 *
 * @author Matheus F. N. Pereira
 */
export interface ErroValidacaoDTO {
  /**
   * O objeto de erro padrão contendo os dados gerais da falha (status, título, etc.).
   */
  erro: ErroPadraoDTO;

  /**
   * A lista de erros de campo específicos que causaram a falha na validação.
   */
  erros: CampoMensagemDTO[];
}
