import { InstituicaoFinanceira } from '../../enums/InstituicaoFinanceira';

/**
 * DTO (Interface) que espelha os dados de requisição
 * para criação e atualização de contas (Backend).
 *
 * Utilizado no corpo (body) dos métodos POST e PUT.
 *
 * @author Matheus F. N. Pereira
 */
export interface CriarAtualizarContaDTO {
  /**
   * Nome/Apelido da conta.
   */
  nome: string;

  /**
   * Banco ou Instituição Financeira.
   */
  instituicao: InstituicaoFinanceira;

  /**
   * Saldo inicial configurado na criação.
   */
  saldoInicial: number;

  /**
   * ID da pessoa titular vinculada
   */
  pessoaId: string;
}
