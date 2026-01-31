import { InstituicaoFinanceira } from '../../enums/InstituicaoFinanceira';
import { PessoaDTO } from '../pessoa/PessoaDTO';

/**
 * DTO que representa uma Conta Bancária no sistema.
 *
 * @author Matheus F. N. Pereira
 */
export interface ContaDTO {
  /**
   * UUID da conta.
   */
  id: string;

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
   * Saldo atual calculado.
   */
  saldoAtual: number;

  /**
   * Pessoa titular da Conta.
   */
  pessoa: PessoaDTO;
}
